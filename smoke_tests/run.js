import {Rate} from 'k6/metrics'
import { b64decode } from 'k6/encoding';

export { mainSmokeTest } from './smoke-tests.js'

export const errorRate = new Rate('errors')

const ExecutionOptions_Scenarios = getScenarios()

getCerts()

export const options = {
    scenarios: ExecutionOptions_Scenarios,
    thresholds: {
        checks: ['rate==1'], // http errors should be less than 1%
    },
}

export function getScenarios() {
    return {
        smokeTest: {
            executor: 'shared-iterations',
            exec: 'mainSmokeTest',
            vus: 1,
            iterations: 1,
            maxDuration: '30s',
        },
    }
}

export function getCerts() {
  console.log("\n[Setup] Retrieving certificates from context");

  // Retrieve environment variables
  const mtlsCertBase64 = __ENV.MTLS_CERT;
  const mtlsKeyBase64 = __ENV.MTLS_KEY;

  console.log(__ENV.SERVICE_URL);


  if (!mtlsCertBase64 || !mtlsKeyBase64) {
    console.error("Error: MTLS_CERT or MTLS_KEY environment variables not set.");
    return; // Exit setup if environment variables are missing
  }

  try {
    // Decode Base64 and write to files
    const decodedCert = b64decode(mtlsCertBase64);
    const decodedKey = b64decode(mtlsKeyBase64);

    open('/client.pem', 'w').write(decodedCert);
    open('/client.key', 'w').write(decodedKey);

    console.log("[Setup] Certificates retrieved\n");

    // You might want to return some data from setup to be used in VU code
    return {
      clientCertPath: '/client.pem',
      clientKeyPath: '/client.key',
    };
  } catch (error) {
    const stringifyError = JSON.stringify(error.message)
    console.error("Error during certificate setup:", stringifyError);
    return; // Exit setup on error
  }
}
