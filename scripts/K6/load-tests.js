import http from 'k6/http';
import { read_certificate } from "./support.js"
import { tagWithCurrentStageIndex } from 'https://jslib.k6.io/k6-utils/1.3.0/index.js';


/***********
 To run this script locally, make sure the following environment variables are set:-

 - RPS = the number of requests per second
 - DURATION = the duration of the test
 - DOMAIN = the fully qualified DNS name of the API server
 - PROFILE = which testing profile to use (MAIN | LAO | NOPERMS | NOCERT)
 - API_KEY = API key that has full access to the API
 - CERT = certificate (either base 64 encoded or name of a file)
 - PRIVATE_KEY = private key for certificate (either base 64 encoded or name of a file)

  - e.g. from K6 folder
  - k6 run -e DOMAIN=dev.integration-api.hmpps.service.justice.gov.uk -e PROFILE=DEV_11_RPS_20_SECONDS -e API_KEY=XXX -e CERT=../path-to/client.pem -e PRIVATE_KEY=../path-to/client.key load-tests.js

 The script is run using the k6 utility from https://k6.io/

 Note that because TLS certificates are defined globally for k6 scripts, we need to launch
 k6 separately for each certificate we want to test with.
***********/

const domain = __ENV.DOMAIN;
const profile = __ENV.PROFILE;
const [cert, key, api_key] = read_certificate();

export const options = (cert === "") ? {} : {
  scenarios: {
    progressive_load: {
      executor: 'ramping-arrival-rate',
      startRate: 0,
      timeUnit: '1s',
      preAllocatedVUs: 1,
      maxVUs: 20,
      stages: [
        { duration: '10s', target: 5 },   // Ramp to 5 rps for 10s
        { duration: '10s', target: 15 },   // Ramp to 15 rps for 10s
        { duration: '10s', target: 5 },    // Ramp down to 5 rps for 10s
      ],
    },
  },
  thresholds: {
    'http_req_failed{stage:1}': ['rate==0.00'],
    'http_req_failed{stage:2}': ['rate>0.00'],
    'http_req_failed{stage:3}': ['rate==0.00'],
    'http_req_failed{status:429}': ['rate>0.00'], //Check that all of the failures from stage 2 are 429s
  },
  tlsAuth: [
    {
      cert,
      key,
    },
  ],
};

const baseUrl = `https://${domain}`;

/**
 * Calls the status endpoint
 */
function call_status_endpoint(path) {
  tagWithCurrentStageIndex()
  http.get(`${baseUrl}/v1/status`, {
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': api_key,
    },
  });
}

/**
 * Validates the status endpoint
 *
 * @returns true if the status endpoint worked
 */

/************************************************************************/

export default function ()  {
  switch (profile) {
    case "PROGRESSIVE_LOAD":
      call_status_endpoint();
      break
    default:
      console.log(`Unsupported profile: ${profile}`);
      break
  }
};

/************************************************************************/
