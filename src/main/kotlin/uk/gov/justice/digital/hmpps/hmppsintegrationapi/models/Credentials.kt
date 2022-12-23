package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.util.Base64

data class Credentials(val username: String?, val password: String?) {
  // Converts credentials to base64 for basic authentication
  fun toBase64(): String? {
    if (username == null || password == null) {
      return null
    }

    return Base64.getEncoder().encodeToString("$username:$password".toByteArray())
  }
}
