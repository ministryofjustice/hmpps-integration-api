import http from 'k6/http';
import { check } from 'k6';

// export const options = {
//   tlsAuth: [
//     {
//       domains: [__ENV.SERVICE_URL], // Specify the domain(s) requiring mTLS
//       cert: open('/client.pem'),
//       key: open('/client.key'),
//     },
//   ],
// };

const get_urls = [
    'https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/X828566',
    'https://www.google.com'
];


export function mainSmokeTest() {
    get_urls.forEach((url) => {
        const res = http.get(url, {
            tlsAuth: [
              {
                cert: '/client.pem',
                key: '/client.key',
              },
            ],
          })
        check(res, {'status code MUST be 200': (res) => res.status === 200})

    });
}
