import http from 'k6/http';
import { group, check, fail } from 'k6';
import exec from 'k6/execution';
import { read_certificate } from "./support.js"

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
const [rps, duration] = set_rate_and_duration()

export const options = (cert === "") ? {} : {
  scenarios: {
    constant_request_rate: {
      executor: 'constant-arrival-rate',
      rate: rps, // X requests
      timeUnit: '1s', // per second
      duration: `${duration}s`, // for X seconds
      preAllocatedVUs: 1, // Pre-allocate VUs to handle the load
    },
  },
  vus: 1,
  tlsAuth: [
    {
      cert,
      key,
    },
  ],
};

const baseUrl = `https://${domain}`;

const httpParams = {
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': api_key,
  },
};

/**
 * Make a GET request to the API and validate that the http response code indicates success.
 * @returns the http response object
 */
function validate_get_request(path) {
  const res = http.get(`${baseUrl}${path}`, httpParams);

  if(res.status !== 200) {
    console.log(`${baseUrl}${path} returned ${res.status}`);
  }
  return res;
}

/**
 * Validates the status endpoint
 *
 * @returns true if the status endpoint worked
 */
function verify_system_endpoints() {
  validate_get_request("/v1/status");
  return true
}


function set_rate_and_duration(){
  switch (profile) {
    case "DEV_11_RPS_20_SECONDS":
      return ["11", "20"];
    default:
      return ["1", "2"];
  }
}

/************************************************************************/

export default function ()  {
  switch (profile) {
    case "DEV_11_RPS_20_SECONDS":
      verify_system_endpoints();
      break
    default:
      console.log(`Unsupported profile: ${profile}`);
      break
  }
};

/************************************************************************/
