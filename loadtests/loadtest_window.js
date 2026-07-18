import http from 'k6/http';
import { check } from 'k6';
import { Rate } from 'k6/metrics';

const BASE      = __ENV.BASE_URL  || 'http://localhost:8083';
const STREAM_ID = __ENV.STREAM_ID || '1';
const VUS       = parseInt(__ENV.VUS || '10');
const DURATION  = __ENV.DURATION  || '30s';

export const options = {
  vus: VUS,
  duration: DURATION,
  summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

const errors = new Rate('errors');
const url     = `${BASE}/api/output/window`;
const payload = JSON.stringify({ streamId: Number(STREAM_ID) });
const params  = { headers: { 'Content-Type': 'application/json' } };

export default function () {
  const res = http.post(url, payload, params);
  const ok = check(res, { 'status is 200': (r) => r.status === 200 });
  errors.add(!ok);
}
