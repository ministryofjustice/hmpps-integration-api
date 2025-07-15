const http = require('k6/http');
const { check } = require('k6');
import encoding from 'k6/encoding';
import exec from 'k6/execution';

const cert = encoding.b64decode(__ENV.LIMITED_ACCESS_CERT, 'std', 's');
const key = encoding.b64decode(__ENV.LIMITED_ACCESS_KEY, 'std', 's');

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A4433DZ';
const allowed_endpoint = `/v1/persons/${hmppsId}/name`;
const not_allowed_endpoint = `/v1/persons?first_name=john`;

export const options = {
  tlsAuth: [
    {
      domains: ["dev.integration-api.hmpps.service.justice.gov.uk"],
      cert,
      key,
    },
  ],
};

export default function ()  {
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': __ENV.LIMITED_ACCESS_API_KEY,
    },
  };

  const res1 = http.get(`${baseUrl}${allowed_endpoint}`, params);
  if (!check(res1, {
    'ALLOWED: returns 200': (r) => r.status === 200,
  })){
    exec.test.fail(`${allowed_endpoint} caused the test to fail`)
  }

  const res2 = http.get(`${baseUrl}${not_allowed_endpoint}`, params);
  if (!check(res2, {
    'NOT ALLOWED: returns 403': (r) => r.status === 403,
  })){
    exec.test.fail(`${not_allowed_endpoint} caused the test to fail`)
  }
};
