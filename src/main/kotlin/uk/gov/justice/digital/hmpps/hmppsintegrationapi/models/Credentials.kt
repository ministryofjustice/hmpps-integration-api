package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.util.Base64

data class Credentials(val username: String, val password: String) {
  fun toBasicAuth(): String {
    val decodedUsername = String(Base64.getDecoder().decode(username)).trim()
    val decodedPassword = String(Base64.getDecoder().decode(password)).trim()

    val encodedCredentials = Base64.getEncoder().encodeToString("$decodedUsername:$decodedPassword".toByteArray())

    return "Basic $encodedCredentials"
  }
}
