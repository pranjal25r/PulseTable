#!/usr/bin/env python3
"""Checks that PulseTable summaries describe the current window, not the whole stream.

Run against the Docker stack (docker compose up --build):

    python3 tests/window_summary_check.py

It creates fresh streams (unique names, so it can be rerun), ingests a CSV with one
numeric column holding 1..20, and compares the summaries with what the window holds.
Takes about half a minute. Exits non-zero if any check fails.

For a manual (non-Docker) run set CSV_CONTAINER="" so the CSV is read from local disk.
"""
import json
import os
import subprocess
import sys
import tempfile
import time
import urllib.request

SDMS = os.environ.get("SDMS_URL", "http://localhost:8081")
INPUT = os.environ.get("INPUT_URL", "http://localhost:8085")
OUTPUT = os.environ.get("OUTPUT_URL", "http://localhost:8083")
# InputMonitor reads the CSV from its own filesystem, so in Docker the file is copied in.
CSV_CONTAINER = os.environ.get("CSV_CONTAINER", "pulsetable-inputmonitor")

VALUES = list(range(1, 21))
RUN_ID = str(int(time.time()))
failures = 0


def post(url, body):
    req = urllib.request.Request(url, data=json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=120) as res:
        text = res.read().decode()
    try:
        return json.loads(text)
    except ValueError:
        return text


def get(url):
    with urllib.request.urlopen(url, timeout=30) as res:
        return json.loads(res.read().decode())


def check(label, expected, actual):
    global failures
    ok = expected == actual
    if not ok:
        failures += 1
    print(f"  [{'PASS' if ok else 'FAIL'}] {label}: expected {expected}, actual {actual}")


def stats(values):
    return {"sum": float(sum(values)), "avg": sum(values) / len(values),
            "min": float(min(values)), "max": float(max(values)), "row_count": len(values)}


def summary_stats(row):
    return {k: row.get(k) for k in ("sum", "avg", "min", "max", "row_count")}


def make_csv():
    local = os.path.join(tempfile.gettempdir(), f"wsc_{RUN_ID}.csv")
    with open(local, "w") as f:
        f.write("val\n" + "\n".join(str(v) for v in VALUES) + "\n")
    if not CSV_CONTAINER:
        return local
    remote = f"/tmp/wsc_{RUN_ID}.csv"
    subprocess.run(["docker", "cp", local, f"{CSV_CONTAINER}:{remote}"], check=True)
    return remote


def create_stream(name, window_type, size, velocity, csv_path, agg_functions):
    stream_id = post(f"{SDMS}/ingestion/stream-master", {
        "streamName": name, "windowType": window_type, "windowSize": size,
        "windowVelocity": velocity, "dataSourceType": "csv", "dataSourcePath": csv_path})["streamId"]
    post(f"{SDMS}/ingestion/stream-col", {
        "stream": {"streamId": stream_id}, "streamColName": "val", "streamColDataType": "integer"})
    col_id = get(f"{SDMS}/ingestion/stream-col/{stream_id}")[0]["streamColId"]
    for agg in agg_functions:
        post(f"{SDMS}/ingestion/stream-query", {
            "stream": {"streamId": stream_id}, "streamCol": {"streamColId": col_id}, "aggFunction": agg})
    post(f"{SDMS}/api/tables/create-table/{stream_id}", {})
    return stream_id, col_id


def column_summary(stream_id, col_id, agg_function=None):
    body = {"streamId": stream_id, "colId": col_id}
    if agg_function:
        body["aggFunction"] = agg_function
    # ordered oldest first; row 0 is the empty seed row written when the table is created
    return post(f"{OUTPUT}/api/output/columnsummary", body)


def main():
    csv_path = make_csv()
    all_aggs = ["SUM", "AVG", "MIN", "MAX"]

    print("1. Count-based window: size 5, velocity 2, values 1..20")
    stream_id, col_id = create_stream(f"wsc_count_{RUN_ID}", "sizetype", 5, 2, csv_path, all_aggs)
    post(f"{INPUT}/api/insert-batched", {"stream_id": stream_id})
    ticks = column_summary(stream_id, col_id)[1:]
    check("number of summaries (one per tick)", 10, len(ticks))
    check("latest summary (window = 16..20)",
          {"sum": 90.0, "avg": 18.0, "min": 16.0, "max": 20.0, "row_count": 5}, summary_stats(ticks[-1]))
    check("mid-stream summary, tick 5 (window = 6..10)",
          {"sum": 40.0, "avg": 8.0, "min": 6.0, "max": 10.0, "row_count": 5}, summary_stats(ticks[4]))
    wrong = [i + 1 for i, row in enumerate(ticks)
             if summary_stats(row) != stats(VALUES[:2 * (i + 1)][-5:])]
    check("ticks whose summary differs from the last 5 rows ingested", [], wrong)

    print("2. Time-based window: 5 s, velocity 2, values 1..20")
    stream_id, col_id = create_stream(f"wsc_time_{RUN_ID}", "timetype", 5, 2, csv_path, all_aggs)
    post(f"{INPUT}/api/insert-batched", {"stream_id": stream_id})
    window = [row["val"] for row in post(f"{OUTPUT}/api/output/window", {"streamId": stream_id})]
    latest = column_summary(stream_id, col_id)[-1]
    print(f"  rows left in window table: {sorted(window)}")
    check("old rows were evicted (window smaller than the stream)", True, 0 < len(window) < len(VALUES))
    check("latest summary equals the rows left in the window table", stats(window), summary_stats(latest))

    print("3. Aggregation choice (stream_queries.agg_function)")
    seed = column_summary(stream_id, col_id, "SUM")[-1]
    check("asking for SUM on a column with all four returns only sum",
          ["sum"], sorted(k for k in seed if k in ("sum", "avg", "min", "max")))
    stream_id, col_id = create_stream(f"wsc_agg_{RUN_ID}", "sizetype", 5, 2, csv_path, ["MAX"])
    post(f"{INPUT}/api/insert-batched", {"stream_id": stream_id})
    latest = column_summary(stream_id, col_id)[-1]
    check("a column configured with MAX only returns only max",
          {"max": 20.0}, {k: latest[k] for k in latest if k in ("sum", "avg", "min", "max")})

    print("FAILED" if failures else "ALL CHECKS PASSED")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
