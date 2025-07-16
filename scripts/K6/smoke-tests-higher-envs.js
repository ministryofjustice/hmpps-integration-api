const http = require('k6/http');
const { check } = require('k6');
import encoding from 'k6/encoding';
import exec from 'k6/execution';

const cert = encoding.b64decode(__ENV.SMOKE_TEST_CERT, 'std', 's');
const key = encoding.b64decode(__ENV.SMOKE_TEST_KEY, 'std', 's');

export const options = {
  tlsAuth: [
    {
      domains: [`${__ENV.DOMAIN}`],
      cert,
      key,
    },
  ],
};

const baseUrl = `https://${__ENV.DOMAIN}`;

const get_endpoints = [
  `/v1/status`
];

export default function ()  {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': __ENV.SMOKE_TEST_API_KEY,
    },
  };

  for (const endpoint of get_endpoints) {
    const res = http.get(`${baseUrl}${endpoint}`, params);
    if (!check(res, {
      [`GET ${endpoint} returns 200`]: (r) => r.status === 200,
    })) {
      exec.test.fail(`${endpoint} caused the test to fail`)
    }
  }
};
