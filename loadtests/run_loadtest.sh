#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://localhost:8083}"
DURATION="${DURATION:-30s}"
LEVELS="${LEVELS:-10 50 100}"
if [ -z "${STREAM_ID:-}" ]; then echo "Set STREAM_ID, e.g. STREAM_ID=1 ./run_loadtest.sh"; exit 1; fi
for VUS in $LEVELS; do
  echo "==================== $VUS concurrent clients ===================="
  VUS="$VUS" DURATION="$DURATION" BASE_URL="$BASE_URL" STREAM_ID="$STREAM_ID" k6 run loadtest_window.js
  echo; sleep 3
done
