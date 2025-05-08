const http = require('k6/http');
const { check } = require('k6');
import encoding from 'k6/encoding';
import exec from 'k6/execution';

const cert = encoding.b64decode(__ENV.NO_ACCESS_CERT, 'std', 's');
const key = encoding.b64decode(__ENV.NO_ACCESS_KEY, 'std', 's');

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const notAllowedEndpoint = `/v1/persons?first_name=john`;

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
  const res = http.get(`${baseUrl}${notAllowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
  });

  if (!check(res, {
    'Cert-based no-access returns 403': (r) => r.status === 403,
  })){
    exec.test.fail(`${notAllowedEndpoint} caused the test to fail`)
  }
};
