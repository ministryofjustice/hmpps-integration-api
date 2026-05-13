package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
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
          .parse(jwtToken) as Jws<Claims>
      jwtDecoded.payload["kid"] = jwtDecoded.header["kid"] // Add kid value as thats in the header not playload
      jwtDecoded.payload
    } catch (e: Exception) {
      null // return null as it fails decode passed in token
    }
}
