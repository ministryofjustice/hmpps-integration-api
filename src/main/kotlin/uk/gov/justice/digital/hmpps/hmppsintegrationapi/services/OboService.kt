package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component

@Component
class OboService {
  fun decodeJwt(jwtToken: String?): Claims? =
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
