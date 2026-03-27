import http from 'k6/http';
import { group, check, fail } from 'k6';
import exec from 'k6/execution';
import { read_certificate } from "./support.js"

/***********
 To run this script locally, make sure the following environment variables are set:-

 - RPS = the number of requests per second
 - DURATION = the duration of the test
 - DOMAIN = the fully qualified DNS name of the API server
 - HMPPSID = the primary hmppsId to use for testing
 - PROFILE = which testing profile to use (MAIN | LAO | NOPERMS | NOCERT)
 - API_KEY = API key that has full access to the API
 - CERT = certificate (either base 64 encoded or name of a file)
 - PRIVATE_KEY = private key for certificate (either base 64 encoded or name of a file)


 The script is run using the k6 utility from https://k6.io/

 Note that because TLS certificates are defined globally for k6 scripts, we need to launch
 k6 separately for each certificate we want to test with.
***********/

const domain = __ENV.DOMAIN;
const profile = __ENV.PROFILE;
const rps = __ENV.RPS;
const duration = __ENV.DURATION;

const [cert, key, api_key] = read_certificate();

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
  return res;
}

/**
 * Validates the status endpoint
 *
 * @returns true if the status endpoint worked
 */
function verify_system_endpoints() {
  let response = validate_get_request("/v1/status");
  if (!check(response, {
    ["Status endpoint reports OK"]: (res) => res.status < 400,
  })) {
    return false
  }
  return true
}

/************************************************************************/

export default function ()  {
  switch (profile) {
    case "MAIN":
      verify_system_endpoints();
      break
    default:
      console.log(`Unsupported profile: ${profile}`);
      break
  }
};

/************************************************************************/
