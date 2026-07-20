# PulseTable

![Java](https://img.shields.io/badge/Java-17%2F21-informational)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-informational)
![Node.js](https://img.shields.io/badge/Node.js-%3E%3D18-informational)
![Vite](https://img.shields.io/badge/Vite-6.x-informational)
![MySQL](https://img.shields.io/badge/MySQL-8.x-informational)

PulseTable is a microservice-based real-time stream aggregation platform built with three Spring Boot services, a React/Vite frontend, and MySQL. You define a stream schema, configure sliding-window size/velocity and an aggregation set (SUM/AVG/MIN/MAX), ingest CSV data in timed batches, and inspect the rolling window and materialized summary output from a dashboard.

## What it does

- Stores stream metadata, columns, and aggregation rules in MySQL.
- Dynamically creates per-stream tables for window rows, summary results, and ingestion bookkeeping.
- Ingests CSV rows in timed batches and applies size-based or time-based windowing.
- Continuously materializes windowed aggregations (SUM/AVG/MIN/MAX) to a summary store.
- Exposes REST APIs for creating streams, starting ingestion, and reading window/summary output.
- Provides a React UI for stream setup, ingestion control, and query/view workflows.

## Architecture

| Module | Role | Port | Stack |
|---|---|---|---|
| `base_frontend/` | React + Vite dashboard UI | 5173 | React, Vite |
| `ingestion_backend/SDMS/` | Stream metadata + dynamic table creation | 8081 | Spring MVC |
| `InputMonitor/` | Batch CSV ingestion worker | 8085 | Spring MVC |
| `outputMonitor/OutputMonitor/` | Read/query APIs for dashboard views | 8083 | Spring WebFlux |
| `Data Mocker Template/` | Optional helper scripts for mock CSV generation | — | Python |

The three backend services share one MySQL database (`sdmsdb`) and coordinate by port. OutputMonitor runs on WebFlux; the other two use the standard Spring MVC stack.

## Performance & Benchmarks

Benchmarks were measured locally on `<your machine — e.g., MacBook Air, Apple M2, 8 GB RAM>`; results will vary by hardware. Reproduction steps are in [Load testing](#load-testing).

### Read/aggregation latency under concurrency

The windowed-aggregation read API (`POST /api/output/window` on OutputMonitor) was load-tested with [k6](https://k6.io) at rising concurrency for 30 s of sustained closed-loop load each. Results at **100 concurrent virtual users**, measured while the stream was ingesting concurrently:

| Metric | Value |
|---|---|
| Concurrency (VUs) | 100 |
| Duration | 30 s sustained |
| Median latency (p50) | **~12 ms** |
| p95 latency | 24 ms |
| p99 latency | 27 ms |
| Max latency | 52 ms |
| Throughput | **~7,500 req/s** |
| Error rate | **0%** (0 / 225,599 requests) |

The tight tail (p95 ≈ 2× median) and zero errors at 100 clients indicate the read path holds up under concurrency rather than degrading. Each request reads the current sliding window (~100 rows) and returns the full aggregation payload.

### Ingestion throughput

Ingesting a 20,000-row CSV took **~85 s (~235 rows/s)**. This is intentionally **paced to the window clock** (10 s windows, velocity 5) — it simulates a real-time feed rather than racing to bulk-load, so it reflects *windowed streaming throughput*, not raw insert speed.

## Database

The application expects a local MySQL database named `sdmsdb`.

The table prefixes `sdb_` and `stream_` are part of the current code and SQL flow and should be kept unless you intentionally change the data model.

## Requirements

- **JDK 21** (recommended for the full stack; the Java services compile and run cleanly on 21)
- **Maven** (a global install is simplest — see the run notes below)
- **MySQL 8.x**
- **Node.js 18+**

## Setup

### 1. MySQL

Start MySQL, set a local password, and create the database:

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '<your_local_password>';
CREATE DATABASE sdmsdb;
FLUSH PRIVILEGES;
```

Set the same password in each service's `application.properties` (`spring.datasource.password`). See the [configuration note](#configuration-note).

### 2. Java

Ensure `java -version` reports 21. On macOS (Homebrew):

```bash
brew install openjdk@21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

## Run the backend services

Start each service in its own terminal. A global Maven install (`brew install maven`) is the simplest path, since not every module ships a complete Maven wrapper. Where `./mvnw` is present and executable you can use it instead.

### Ingestion backend (8081)

```bash
cd ingestion_backend/SDMS
mvn spring-boot:run
```

### InputMonitor (8085)

```bash
cd InputMonitor
mvn spring-boot:run
```

### OutputMonitor (8083)

```bash
cd outputMonitor/OutputMonitor
mvn spring-boot:run
```

Each service is ready when it logs `Started ...Application in N seconds`. Sanity check:

```bash
curl -s http://localhost:8083/ingestion/stream-master
```

An empty array `[]` means the read API is up (no streams created yet).

## Run the frontend

```bash
cd base_frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Generate sample data

A ready-to-run generator (8-column synthetic AQI feed) — run from the `Data Mocker Template/` folder:

```bash
python3 - << 'EOF'
import csv, random
from datetime import datetime, timedelta
ROWS = 20000
start = datetime(2024, 1, 1)
def aqi(i):  return max(0, 100 + 30*(i % 10 >= 5) - 15*(i % 20 == 0) + random.randint(-10, 10))
def pm25(i): return min(500, int(50 + i*0.05 + random.randint(-10, 10)))
def comp(a, v=20): return max(0, int(a + random.gauss(0, v)))
with open("synthetic_aqi.csv", "w", newline="") as f:
    w = csv.writer(f)
    w.writerow(["date","aqi","pm2_5","pm10","no2","so2","co","o3"])
    for i in range(ROWS):
        d = (start + timedelta(minutes=i)).strftime("%Y-%m-%d %H:%M:%S")
        w.writerow([d, aqi(i), pm25(i), comp(80), comp(40), comp(30),
                    round(random.uniform(0.5, 2.0), 2), comp(60)])
print("wrote", ROWS, "rows to synthetic_aqi.csv")
EOF
```

## Typical flow

1. Create a stream from the UI (Window Type, Size, Velocity).
2. Add the stream columns (matching the CSV header, in order) and aggregation rules.
3. Create the stream tables.
4. Start ingestion from InputMonitor.
5. Use the dashboard to inspect the window and summary output.

**Data Source Path must point to the CSV file itself**, e.g. `.../Data Mocker Template/synthetic_aqi.csv` — not the containing folder. Pointing at a directory fails with `Failed to read CSV file ... (Is a directory)`.

## Load testing

Reproduce the latency benchmark with the scripts in `loadtests/`.

**Requirements:** `brew install k6`, plus a stream that has ingested data.

```bash
cd loadtests
STREAM_ID=1 ./run_loadtest.sh
```

`run_loadtest.sh` sweeps `/api/output/window` at 10 → 50 → 100 concurrent clients (30 s each) via `loadtest_window.js`. In each block, under `http_req_duration`:

- `med` = median latency at that concurrency (the headline number)
- `http_reqs .../s` = throughput
- `errors` should be ~0 for the result to count

Report the highest concurrency level that holds with a stable tail and ~0 errors.

## Configuration note

The Spring Boot service configs currently contain a plaintext MySQL password for local development. **Externalize it to an environment variable** before using this outside your machine, e.g. `spring.datasource.password=${DB_PASSWORD}`, and keep `application.properties` out of version control if it holds a real secret.

## Notes

- Keep stream and column names simple and lowercase to avoid SQL naming issues (they become table/column names).
- The app assumes the CSV includes a header row.
- OutputMonitor uses WebFlux; the other services use the standard Spring MVC stack.
- Time-Type windowing keys off a timestamp column, so include one in your schema.
