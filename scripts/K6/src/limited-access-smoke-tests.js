const http = require('k6/http');
const { check } = require('k6');
import encoding from 'k6/encoding';

const cert = encoding.b64decode(__ENV.LIMITED_ACCESS_CERT, 'std', 's');
const key = encoding.b64decode(__ENV.LIMITED_ACCESS_KEY, 'std', 's');

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A8451DY';
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

module.exports.default = function () {
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
};
