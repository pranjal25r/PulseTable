# PulseTable

![CI](https://github.com/pranjal25r/PulseTable/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17%2F21-informational)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-informational)
![Node.js](https://img.shields.io/badge/Node.js-%3E%3D18-informational)
![Vite](https://img.shields.io/badge/Vite-6.x-informational)
![MySQL](https://img.shields.io/badge/MySQL-8.x-informational)
![Docker](https://img.shields.io/badge/Docker-Compose-informational)

PulseTable is a microservice-based real-time stream aggregation platform built with three Spring Boot services, a React/Vite frontend, MySQL, and Docker. You define a stream schema, configure sliding-window size/velocity and an aggregation set (SUM/AVG/MIN/MAX), ingest CSV data in timed batches, and inspect the rolling window and materialized summary output from a dashboard.

## Quickstart (Docker)

The fastest way to run the full stack — no JDK or Node install needed:

```bash
# 1. copy the env template and set your password
cp .env.example .env

# 2. bring up all five services (MySQL + 3 Spring Boot + React/nginx)
docker compose up --build
```

Open `http://localhost:5173`. All five containers start in dependency order — MySQL health-checks pass before the Spring services boot, so the stack comes up cleanly without manual sequencing.

To stop everything:
```bash
docker compose down        # stops containers, keeps the MySQL volume
docker compose down -v     # stops and deletes the volume (fresh DB)
```

> **Manual run** (without Docker) — see [Run the backend services](#run-the-backend-services) below.

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
| `base_frontend/` | React + Vite dashboard UI | 5173 | React, Vite, nginx |
| `ingestion_backend/SDMS/` | Stream metadata + dynamic table creation | 8081 | Spring MVC |
| `InputMonitor/` | Batch CSV ingestion worker | 8085 | Spring MVC |
| `outputMonitor/OutputMonitor/` | Read/query APIs for dashboard views | 8083 | Spring WebFlux |
| `Data Mocker Template/` | Optional helper scripts for mock CSV generation | — | Python |

The three backend services share one MySQL database (`sdmsdb`) and coordinate by port. OutputMonitor runs on WebFlux; the other two use the standard Spring MVC stack. In the Docker setup, all services run on a shared bridge network (`pulsetable-network`) and communicate by service name.

## Docker setup

Each module has its own multi-stage Dockerfile:

- **Spring Boot services** — Stage 1: `eclipse-temurin:21-jdk` + Maven build; Stage 2: `eclipse-temurin:21-jre` runtime. Only the fat JAR is copied into the final image, keeping it lean.
- **Frontend** — Stage 1: `node:18-alpine` + `npm ci` + `npm run build`; Stage 2: `nginx:alpine` serves the compiled `dist/`. Maps to port 5173 on the host.

`docker-compose.yml` at the repo root wires all five services with:
- MySQL 8.4 with a named persistent volume and a `mysqladmin ping` healthcheck
- Spring services that `depends_on` MySQL with `condition: service_healthy` — they only start once the DB is accepting connections
- All credentials read from `.env` (never hardcoded)

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

## Requirements

**Docker path (recommended):**
- Docker Desktop (or Docker Engine + Compose plugin)

**Manual path:**
- JDK 21 (`brew install openjdk@21`)
- Maven (`brew install maven`)
- MySQL 8.x (`brew install mysql`)
- Node.js 18+ (`brew install node`)

## Setup (manual path only)

If running without Docker, set up the dependencies manually.

### 1. MySQL

```bash
brew install mysql && brew services start mysql
mysql -u root
```

```sql
ALTER USER 'root'@'localhost' IDENTIFIED BY '<your_local_password>';
CREATE DATABASE sdmsdb;
FLUSH PRIVILEGES;
EXIT;
```

### 2. Java

```bash
brew install openjdk@21
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
```

Verify: `java -version` should report 21.

## Run the backend services

Start each service in its own terminal. Use `mvn` directly (global install via `brew install maven`) — not every module ships a complete Maven wrapper.

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
2. Add stream columns (matching the CSV header, in order) and aggregation rules.
3. Create the stream tables.
4. Start ingestion from InputMonitor.
5. Use the dashboard to inspect the window and summary output.

**Data Source Path must point to the CSV file itself** — e.g. `.../Data Mocker Template/synthetic_aqi.csv`, not the containing folder. Pointing at a directory fails with `Failed to read CSV file ... (Is a directory)`.

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

## Configuration note

The Spring Boot service configs currently contain a plaintext MySQL password for local development. **Externalize it to an environment variable** before using this outside your machine — e.g. `spring.datasource.password=${DB_PASSWORD}` — and keep `application.properties` out of version control if it holds a real secret. The Docker setup reads credentials from `.env` which is gitignored.

## Notes

- Keep stream and column names simple and lowercase to avoid SQL naming issues (they become table/column names).
- The app assumes the CSV includes a header row.
- OutputMonitor uses WebFlux; the other services use the standard Spring MVC stack.
- Time-Type windowing keys off a timestamp column, so include one in your schema.
- The Maven wrapper (`mvnw`) is incomplete in some modules — use `mvn` directly.