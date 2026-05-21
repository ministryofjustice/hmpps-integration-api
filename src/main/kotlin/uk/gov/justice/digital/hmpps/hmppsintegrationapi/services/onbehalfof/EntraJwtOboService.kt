package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Header
import io.jsonwebtoken.Jwt
import io.jsonwebtoken.Jwts
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.math.BigInteger
import java.net.URI
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.Base64

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
      log.warn("Token failed to decode entra token :$token with error:${e.message}")
      null // return null as it fails decode passed in token
    }

  fun decodeJwt(token: String): Jwt<Header?, Claims?>? =
    Jwts
      .parser()
      .verifyWith(getPublicKey(token))
      .build()
      .parseSignedClaims(token) as Jwt<Header?, Claims?>?

  fun getPublicKey(token: String): PublicKey {
    val (headerPart, payloadPart) = token.split(".")
    val mapper = jacksonObjectMapper()
    val header = mapper.readValue(String(Base64.getUrlDecoder().decode(headerPart)), Map::class.java)
    val payload = mapper.readValue(String(Base64.getUrlDecoder().decode(payloadPart)), Map::class.java)
    val kid = header["kid"] as String
    val iss = payload["iss"] as String
    val tenant = iss.removePrefix("https://login.microsoftonline.com/").substringBefore("/")
    val jwksUrl = "https://login.microsoftonline.com/$tenant/discovery/v2.0/keys"
    val jwks = mapper.readTree(URI(jwksUrl).toURL().openStream())
    val key = jwks["keys"].first { it["kid"].asString() == kid }
    val modulus = Base64.getDecoder().decode(key["n"].asString())
    val exponent = Base64.getDecoder().decode(key["e"].asString())
    val spec =
      RSAPublicKeySpec(
        BigInteger(1, modulus),
        BigInteger(1, exponent),
      )
    return KeyFactory.getInstance("RSA").generatePublic(spec)
  }
}
