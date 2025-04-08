import http from 'k6/http';
import {check} from 'k6';
import encoding from 'k6/encoding';

const API_KEY = __ENV.API_KEY
const mtlsCertBase64 = __ENV.MTLS_CERT;
const mtlsKeyBase64 = __ENV.MTLS_KEY;

const get_urls = [
    'https://dev.integration-api.hmpps.service.justice.gov.uk/v1/persons/X828566',
    'https://www.google.com'
];


export function mainSmokeTest() {
    console.log("HEY HEY -------------------")
  console.log(API_KEY)
  try {
    if(!mtlsCertBase64 || !mtlsKeyBase64) {
        console.error("Error: MTLS_CERT or MTLS_KEY environment variables not set.");
        return; // Exit setup if environment variables are missing
      }
      console.log('hurray!')
  } catch(error) {
      console.log('NOPE')
  }

console.log("-----------------")
  try {
    get_urls.forEach((url) => {
      const res = http.get(url, {
        tlsAuth: [
          {
            domain: 'dev.integration-api.hmpps.service.justice.gov.uk',
            cert: encoding.b64decode(mtlsCertBase64),
            key: encoding.b64decode(mtlsKeyBase64)
          },
        ],
        headers: {
          'x-api-key': API_KEY
        }
      })
      check(res, {'status code MUST be 200': (res) => res.status === 200})

    });
  } catch (error) {
    console.log('Error in request:', JSON.stringify(error.message))
  }
}
