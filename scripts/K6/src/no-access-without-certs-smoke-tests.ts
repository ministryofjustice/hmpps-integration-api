/// <reference types="k6" />
import http from 'k6/http';
import { check } from 'k6';

const baseUrl = 'https://dev.integration-api.hmpps.service.justice.gov.uk';
const hmppsId = 'A8451DY';
const allowedEndpoint = `/v1/persons/${hmppsId}/name`;

export default function () {
  const res = http.get(`${baseUrl}${allowedEndpoint}`, {
    headers: {
      'x-api-key': __ENV.NO_ACCESS_API_KEY,
    },
  });

  check(res, {
    'Request without cert should NOT return 200': (r) => r.status !== 200,
  });
}
