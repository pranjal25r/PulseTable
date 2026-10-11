# PulseTable

![CI](https://github.com/pranjal25r/PulseTable/actions/workflows/ci.yml/badge.svg)

## What it is

PulseTable keeps running totals and averages over the most recent part of a stream of data. For example, if an air-quality sensor sends a new reading every second, PulseTable can show the average pollution level over the last 100 readings or the last 10 seconds, updated every second.

## Why I built it

For data that keeps arriving, an average over everything ever recorded is usually not what you want: old readings drown out what is happening now. I wanted the numbers to describe the latest data only, so each stream keeps a "window" of recent rows and every result is calculated from that window alone.

## How it works

1. **You define a stream.** In the web page you give it a name, its columns (for example `aqi`, `pm2_5`), a window size, how many rows arrive per second, and which of SUM, AVG, MIN and MAX you want for which columns.
2. **Each stream gets its own table.** PulseTable creates a MySQL table for the stream's window, plus one table for its results and one that records how far it has read.
3. **Data is fed in batches every second.** The rows come from a CSV file. Each second the next batch of rows is added to the window table.
4. **Old rows drop out of the window.** A window can be a number of rows ("the last 100 rows") or a length of time ("the last 10 seconds"). Rows that no longer fit are deleted.
5. **SUM, AVG, MIN and MAX are recomputed over the window and shown on the dashboard.** This happens every second, after the old rows are removed, with one SQL query over the window table. The dashboard shows the rows in the window and a table and chart of the results you picked.

## The three services

| Service | Folder | Port | Its job |
|---|---|---|---|
| SDMS | `ingestion_backend/SDMS/` | 8081 | Saves stream definitions and creates each stream's tables. |
| InputMonitor | `InputMonitor/` | 8085 | Reads the CSV, adds rows every second, removes old rows and recomputes the results. |
| OutputMonitor | `outputMonitor/OutputMonitor/` | 8083 | Answers the dashboard's requests for the window and the results. |

They share one MySQL database; the split keeps each part small rather than being fully independent.

All three are Spring Boot (Spring MVC) applications on Java 21. The web page in `base_frontend/` is React, served on port 5173.

## How to run

You need Docker Desktop (or Docker Engine with the Compose plugin).

```bash
cp .env.example .env          # then edit .env and set your own password on both password lines
docker compose up --build
```

Open `http://localhost:5173`. Compose starts MySQL first and waits until it is ready before starting the three services.

The database password is not stored in the code. Each service reads it from the `SPRING_DATASOURCE_PASSWORD` environment variable, which Compose takes from `.env` (not committed; `.env.example` is the template).

```bash
docker compose down        # stop, keep the database
docker compose down -v     # stop and delete the database
```

### Trying it with sample data

`Data Mocker Template/synthetic_aqi.csv` holds 20,000 made-up air-quality readings. To make a fresh copy, run `python3 gen.py` in that folder.

InputMonitor reads the CSV from inside its own container, so copy the file in first:

```bash
docker cp "Data Mocker Template/synthetic_aqi.csv" pulsetable-inputmonitor:/tmp/synthetic_aqi.csv
```

Then in the web page:

1. Create a stream. Choose the window type, size and rows per second, and enter `/tmp/synthetic_aqi.csv` as the data source path. The path must be the CSV file itself, not its folder.
2. Add the columns, with the same names and order as the CSV header (`date, aqi, pm2_5, pm10, no2, so2, co, o3`). Use simple lowercase names, because they become table and column names.
3. Pick the results you want (for example AVG of `aqi`) and create the tables.
4. Start ingestion, then open the dashboard to watch the window and the results.

### Running without Docker

You need JDK 21, Maven, Node.js 18+ and MySQL 8 with a database called `sdmsdb`. In each of three terminals:

```bash
export SPRING_DATASOURCE_PASSWORD='<your MySQL root password>'
cd ingestion_backend/SDMS && mvn spring-boot:run        # or InputMonitor, or outputMonitor/OutputMonitor
```

Then start the web page with `cd base_frontend && npm install && npm run dev`. Use `mvn` rather than `./mvnw`; the Maven wrapper is incomplete in some folders.

## How it's tested

`tests/window_summary_check.py` checks that the results describe the window and not the whole stream. With the Docker stack running:

```bash
python3 tests/window_summary_check.py
```

It feeds the numbers 1 to 20 into a stream with a window of 5 rows, 2 rows per second. At the end the window holds 16, 17, 18, 19, 20, so the results must be **SUM 90, AVG 18, MIN 16, MAX 20**. (Totals over all 20 numbers would be SUM 210 and MIN 1.) It also checks:

- every earlier second against the 5 rows that were in the window at that point;
- a time-based window (5 seconds): the results must equal the rows actually left in the window table;
- only the results you picked for a column are returned;
- a stream named with capitals and a space ("Air Quality") works from creation to results, and unsafe stream and column names are rejected with HTTP 400 without creating any table.

It needs only Python 3 and takes about half a minute. CI builds the three services and the web page on every push; it does not run this check.

## Load test results

How fast OutputMonitor can return the window (`POST /api/output/window`) while a stream is being fed. Measured on 2026-10-10 with [k6](https://k6.io) v2.1.0: 10, 50 and 100 simulated users, each sending requests back to back for 30 seconds. The stream had a window of 100 rows and 5 new rows per second, so every answer contained 100 rows.

| Users at once | Median | 95th percentile | Requests/second | Errors |
|---|---|---|---|---|
| 10 | 1.35 ms | 2.45 ms | 6,532 | 0 of 195,980 |
| 50 | 6.05 ms | 14.20 ms | 7,113 | 0 of 213,425 |
| 100 | 11.73 ms | 32.24 ms | 7,204 | 0 of 216,210 |

Hardware: MacBook Air (model identifier Mac17,3), Apple M5 chip with 10 cores, 16 GB RAM. Everything ran on that one laptop: the services in Docker (limited to 10 CPUs and 7.75 GB) and k6 beside them. Numbers will differ on other machines.

The full k6 output is in [loadtests/results/2026-10-10-spring-mvc.txt](loadtests/results/2026-10-10-spring-mvc.txt). To repeat it, install k6 (`brew install k6`), start feeding a stream, and run:

```bash
cd loadtests
STREAM_ID=<your stream id> ./run_loadtest.sh | tee results/$(date +%F).txt
```

Feeding speed was measured separately, in an earlier run recorded in [loadtests/results/2026-10-10.txt](loadtests/results/2026-10-10.txt): 20,000 rows at 250 rows per second took 90.8 seconds. Feeding is deliberately paced at one batch per second, so this is not a measure of how fast MySQL can insert.

## Known limits

- **One shared database.** All three services read and write the same MySQL database, so they cannot be deployed or scaled separately.
- **Time windows are measured in whole seconds**, so they can hold slightly more rows than expected. In the test, a 5-second window at 2 rows per second held 10 to 12 rows.
- **Stream and column names may only contain letters, digits, spaces and underscores.** They become table and column names, and SQL can't take those as safe parameters, so they are checked when the stream is created.
- **One ingestion at a time.** InputMonitor has a single on/off switch for feeding, so starting or stopping one stream affects any other that is running.
