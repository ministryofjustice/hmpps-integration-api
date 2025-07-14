const http = require('k6/http');
const { check } = require('k6');
import exec from 'k6/execution';

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A3696EC';
const allowedEndpoint = `/v1/persons/${hmppsId}/name`;

export default function () {
  const res = http.get(`${baseUrl}${allowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
    timeout: '2s'
  });

  if (!check(res, {
    'Request without cert should NOT return 200': (r) => r.status !== 200,
  })){
    exec.test.fail(`${allowedEndpoint} caused the test to fail`)
  }
};
