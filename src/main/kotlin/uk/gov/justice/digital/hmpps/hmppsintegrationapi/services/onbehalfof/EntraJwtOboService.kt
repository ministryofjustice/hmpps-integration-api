package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EntraJwtOboService : OboService {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  override fun extractUsername(token: String): String? =
    try {
      val jwtDecoded = decodeJwt(token)
      log.info(
        buildLogMessage(jwtDecoded),
      )
      jwtDecoded?.payload?.get("unique_name").toString()
    } catch (e: Exception) {
      log.warn("Token failed to decode token :$token with error:${e.message}")
      null // return null as it fails decode passed in token
    }

  fun decodeJwt(token: String): Jwt<Header?, Claims?>? =
    Jwts
      .parser()
      .unsecured()
      .build()
      .parseUnsecuredClaims(token)
}

fun buildLogMessage(jwtDecoded: Jwt<Header?, Claims?>?): String =
  "Token found with iss:${jwtDecoded?.payload?.get("iss")}, " +
    "appId:${jwtDecoded?.payload?.get("appid")}, " +
    "unique_name:${jwtDecoded?.payload?.get("unique_name")}, " +
    "kid:${jwtDecoded?.header?.get("kid")}, " +
    "nbf:${jwtDecoded?.payload?.get("nbf")}, " +
    "exp:${jwtDecoded?.payload?.get("exp")}, " +
    "aud:${jwtDecoded?.payload?.get("aud")}"
