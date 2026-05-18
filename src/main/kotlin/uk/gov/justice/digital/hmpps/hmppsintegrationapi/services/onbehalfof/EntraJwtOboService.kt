package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.security.KeyPair

class EntraJwtOboService : OboService {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun extractUsername(token: String): String? =
    try {
      val jwtDecoded = decodeJwt(token, KeyPair(null, null)) // TODO: Replace empty key with public key extraction
      log.info(
        buildLogMessage(jwtDecoded),
      )
      jwtDecoded?.payload?.get("unique_name").toString()
    } catch (e: Exception) {
      log.warn("Token failed to decode entra token :$token with error:${e.message}")
      null // return null as it fails decode passed in token
    }

  fun decodeJwt(
    token: String,
    key: KeyPair,
  ): Jwt<Header?, Claims?>? =
    Jwts
      .parser()
      .verifyWith(key.public)
      .build()
      .parseSignedClaims(token) as Jwt<Header?, Claims?>?
}
