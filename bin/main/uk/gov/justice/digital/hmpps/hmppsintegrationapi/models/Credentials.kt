package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.util.Base64

data class Credentials(val username: String, val password: String) {
  fun toBasicAuth(): String {
    val encodedCredentials = Base64.getEncoder().encodeToString("$username:$password".toByteArray())
    return "Basic $encodedCredentials"
  }
}
