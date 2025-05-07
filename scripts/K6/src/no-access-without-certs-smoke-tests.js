const http = require('k6/http');
const { check } = require('k6');

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A8451DY';
const allowedEndpoint = `/v1/persons/${hmppsId}/name`;

module.exports.default = function () {
  const res = http.get(`${baseUrl}${allowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
    timeout: '2s'
  });

  check(res, {
    'Request without cert should NOT return 200': (r) => r.status !== 200,
  });
};
