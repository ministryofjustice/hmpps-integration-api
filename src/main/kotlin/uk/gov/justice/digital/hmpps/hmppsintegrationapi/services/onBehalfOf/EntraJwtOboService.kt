package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onBehalfOf

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts

class EntraJwtOboService : OboService {
  override fun decodeJwt(jwtToken: String): Claims? =
    try {
      val jwtDecoded =
        Jwts
          .parser()
          .build()
          .parseUnsecuredClaims(jwtToken)
      jwtDecoded.payload["kid"] = jwtDecoded.header["kid"] // Add kid value as that's in the header not payload
      jwtDecoded.payload
    } catch (e: Exception) {
      null // return null as it fails decode passed in token
    }
}
