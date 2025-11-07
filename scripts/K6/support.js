import encoding from 'k6/encoding';

function read_or_decode(value) {
  if (value === "") {
    // Empty values are legitimate for the "no cert" test
    return "";
  }

  if (value.endsWith(".pem") || value.endsWith(".key")) {
    // Value might be the name of a file, so read the file
    value = open(value);
  }

  if (value.startsWith("-----BEGIN ")) {
    // Value might be a unencoded PEM content
    return value
  }

  // Otherwise, assume base64 encoded content
  return encoding.b64decode(value, 'std', 's');
}

function safer(value) {
  if (value == null) {
    return ""
  }
  return value.trimEnd()
}

export function read_certificate() {
  return [
    read_or_decode(__ENV.CERT),
    read_or_decode(__ENV.PRIVATE_KEY),
    safer(__ENV.API_KEY)
  ]
}


