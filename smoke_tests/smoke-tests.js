import http from 'k6/http';
import { check } from 'k6';

const API_KEY = __ENV.API_KEY

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
    console.log("HEY HEY -------------------")
  console.log(API_KEY)
  // console.log(is)
  try {
      const file = open('/client.pem')
    file.close();
      console.log('hurray!')
  } catch(error) {
      console.log('NOPE')
  }

console.log("-----------------")
    get_urls.forEach((url) => {
        const res = http.get(url, {
            tlsAuth: [
              {
                cert: '/client.pem',
                key: '/client.key',
              },
            ],
          headers: {
              'x-api-key': API_KEY
          }
          })
        check(res, {'status code MUST be 200': (res) => res.status === 200})

    });
}
