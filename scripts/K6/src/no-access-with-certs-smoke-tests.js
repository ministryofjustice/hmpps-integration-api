const http = require('k6/http');
const { check } = require('k6');

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const notAllowedEndpoint = `/v1/persons?first_name=john`;

module.exports.options = {
  tlsAuth: [
    {
      domains: ['dev.integration-api.hmpps.service.justice.gov.uk'],
      cert: open('/tmp/no_access.pem'),
      key: open('/tmp/no_access.key'),
    },
  ],
};

module.exports.default = function () {
  const res = http.get(`${baseUrl}${notAllowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
  });

  check(res, {
    'Cert-based no-access returns 403': (r) => r.status === 403,
  });
};
