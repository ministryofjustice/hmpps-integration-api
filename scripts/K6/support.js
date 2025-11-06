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

function safer(value) {
  if (value == null) {
    return ""
  }
  return value.trimEnd()
}

export function read_certificate() {
  let cert_val = safer(__ENV.CERT)
  let key_val = safer(__ENV.PRIVATE_KEY)
  let api_key_val = safer(__ENV.API_KEY)

  return [
    read_or_decode(cert_val, ".pem"),
    read_or_decode(key_val, ".key"),
    api_key_val
  ]
}


