# PulseTable

![Java](https://img.shields.io/badge/Java-17%2F21-informational)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-informational)
![Node.js](https://img.shields.io/badge/Node.js-%3E%3D18-informational)
![Vite](https://img.shields.io/badge/Vite-6.x-informational)

PulseTable is a microservice-based streaming data demo built with three Spring Boot services, a React/Vite frontend, and MySQL. It lets you define a stream schema, configure windowing and aggregations, ingest CSV data in batches, and inspect the rolling window and summary output from a dashboard.

## What it does

- Stores stream metadata, columns, and aggregation rules in MySQL.
- Creates per-stream tables for window rows, summary results, and ingestion bookkeeping.
- Ingests CSV rows in timed batches and applies size-based or time-based windowing.
- Exposes APIs for creating streams, starting ingestion, and reading window or summary output.
- Provides a React UI for stream setup, ingestion control, and query/view workflows.

## Modules

- `base_frontend/` - React + Vite UI on `http://localhost:5173`
- `ingestion_backend/SDMS/` - stream metadata and dynamic table creation on `http://localhost:8081`
- `InputMonitor/` - batch CSV ingestion worker on `http://localhost:8085`
- `outputMonitor/OutputMonitor/` - read/query APIs for dashboard views on `http://localhost:8083`
- `Data Mocker Template/` - optional helper scripts for mock CSV generation

## Database

The application expects a local MySQL database named `sdmsdb`.

The table prefixes `sdb_` and `stream_` are part of the current code and SQL flow and should be kept unless you intentionally change the data model.

## Configuration note

The Spring Boot service configs currently contain a plaintext MySQL password for local development. Externalize it to an environment variable before using this outside your machine.

## Requirements

- MySQL 8.x
- JDK 21 is recommended for the full stack; JDK 17 is sufficient for the services that currently build against it
- Node.js 18+

## Run the backend services

Start each service in its own terminal.

### Ingestion backend

```bash
cd ingestion_backend/SDMS
./mvnw spring-boot:run
```

### InputMonitor

```bash
cd InputMonitor
./mvnw spring-boot:run
```

### OutputMonitor

```bash
cd outputMonitor/OutputMonitor
./mvnw spring-boot:run
```

## Run the frontend

```bash
cd base_frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

## Typical flow

1. Create a stream from the UI.
2. Add the stream columns and aggregation rules.
3. Create the stream tables.
4. Start ingestion from InputMonitor.
5. Use the dashboard to inspect the window and summary output.

## Notes

- Keep stream and column names simple and lowercase to avoid SQL naming issues.
- The app currently assumes the CSV includes a header row.
- OutputMonitor uses WebFlux while the other services use the standard Spring MVC stack.
