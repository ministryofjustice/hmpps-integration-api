import http from "k6/http";
import encoding from 'k6/encoding';

function read_or_decode(value, suffix) {
  if (value === "") {
    return "";
  }
  if (value.includes(suffix)) {
    return open(value);
  }
  return encoding.b64decode(value, 'std', 's');
}

export function read_certificate(profile) {
  let cert_val = ""
  let key_val = ""
  let api_key_val = ""
  switch (profile) {
    case "NOCERT":
      cert_val = "";
      key_val = "";
      api_key_val = __ENV.NO_ACCESS_API_KEY;
      break
    case "PROD":
      cert_val = __ENV.SMOKE_TEST_CERT;
      key_val = __ENV.SMOKE_TEST_KEY;
      api_key_val = __ENV.SMOKE_TEST_API_KEY;
      break
    case "MAIN":
    case "MINIMAL":
    case "NOPERMS":
    case "LIMITED":
    case "REVOKED":
      let api_key = __ENV.API_KEY.trimEnd()
      cert_val = __ENV.CERT;
      key_val = __ENV.PRIVATE_KEY;
      api_key_val = api_key;
      break
    default:
      console.log("Unknown profile: " + profile);
  }

  return [
    read_or_decode(cert_val, ".pem"),
    read_or_decode(key_val, ".key"),
    api_key_val
  ]
}


