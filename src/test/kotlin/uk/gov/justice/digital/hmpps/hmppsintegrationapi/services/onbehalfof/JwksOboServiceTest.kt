package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Jwks
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import java.net.URL
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.Date

class JwksOboServiceTest :
  DescribeSpec(
    {
      val kid = "ABC-123"

      val keyPair = generateKeyPair()
      val jwksUri = buildJwks(keyPair, kid)
      val service = JwksOboService(jwksUri)

      it("loads the JWKS") {
        service.loadJwks()
      }

      it("parses a JWT using JWKS") {
        val jwt = makeJwt(kid, "tester1", keyPair.private)
        val jwtUser = service.extractUsername(jwt)
        jwtUser shouldBe "tester1"
      }

      it("fails if KID not found") {
        val jwt = makeJwt("XYZ-987", "tester2", keyPair.private)
        shouldThrow<UnsupportedJwtException> {
          service.extractUsername(jwt)
        }
      }
    },
  )

private fun makeJwt(
  kid: String,
  username: String,
  signingKey: PrivateKey,
): String =
  Jwts
    .builder()
    .header()
    .keyId(kid)
    .and()
    .subject(username)
    .issuedAt(Date.from(Instant.now()))
    .expiration(Date.from(Instant.now().plusSeconds(3600)))
    .claim("unique_name", username)
    .signWith(signingKey)
    .compact()!!

private fun generateKeyPair(): KeyPair {
  val keyGenerator = KeyPairGenerator.getInstance("RSA")
  keyGenerator.initialize(2048)
  val keyPair = keyGenerator.generateKeyPair()
  return keyPair!!
}

private fun DescribeSpec.buildJwks(
  keyPair: KeyPair,
  kid: String,
): URL {
  val jwk =
    Jwks
      .builder()
      .key(keyPair.public as RSAPublicKey)
      .id(kid)
      .algorithm("RS256")
      .build()

  val wrapper = mapOf("keys" to listOf(jwk))
  val jwksText = ObjectMapper().writeValueAsString(wrapper)
  val jwksFile = tempfile("testing", "jwks")
  jwksFile.deleteOnExit()
  jwksFile.writeText(jwksText)
  val uri = jwksFile.toURI().toURL()

  return uri
}
