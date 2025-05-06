/// <reference types="k6" />

import http from 'k6/http';
import { check } from 'k6';

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A8451DY';
const allowed_endpoint = `/v1/persons/${hmppsId}/name`;
const not_allowed_endpoint = `/v1/persons?first_name=john`;

export const options = {
  tlsAuth: [
    {
      domains: [new URL(baseUrl).hostname],
      cert: open('/tmp/limited_access.pem'),
      key: open('/tmp/limited_access.key'),
    },
  ],
};

console.log("Beginning limited access smoke tests - first endpoint should return 200, second should return 403")
export default function () {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': __ENV.LIMITED_ACCESS_API_KEY,
    },
  };

  const res1 = http.get(`${baseUrl}${allowed_endpoint}`, params);
  check(res1, {
    'ALLOWED: returns 200': (r) => r.status === 200,
  });

  const res2 = http.get(`${baseUrl}${not_allowed_endpoint}`, params);
  check(res2, {
    'NOT ALLOWED: returns 403': (r) => r.status === 403,
  });
}
console.log("Completed limited access smoke tests")

