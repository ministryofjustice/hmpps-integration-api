/// <reference types="k6" />
import http from 'k6/http';
import { check } from 'k6';

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const notAllowedEndpoint = `/v1/persons?first_name=john`;

export const options = {
  tlsAuth: [
    {
      domains: ['dev.integration-api.hmpps.service.justice.gov.uk'],
      cert: open('/tmp/no_access.pem'),
      key: open('/tmp/no_access.key'),
    },
  ],
};

export default function () {
  const res = http.get(`${baseUrl}${notAllowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
  });

  check(res, {
    'Cert-based no-access returns 403': (r) => r.status === 403,
  });
}
