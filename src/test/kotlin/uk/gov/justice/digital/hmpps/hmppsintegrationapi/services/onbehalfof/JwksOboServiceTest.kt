package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Jwks
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.shouldBe
import java.net.URI
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
      val service = JwksOboService(jwksUri, "subject")

      it("loads the JWKS") {
        service.keyCount() shouldBe 0
        service.keyStatus() shouldBe JwksOboService.KeyStatus.NEW

        service.loadJwks()

        service.keyCount() shouldBe 1
        service.keyStatus() shouldBe JwksOboService.KeyStatus.LOADED
      }

      it("parses a JWT using JWKS") {
        val jwt = makeJwt(kid, "tester1", keyPair.private)
        val jwtUser = service.extractUsername(jwt)
        jwtUser shouldBe "tester1"
      }

      it("fails if KID not found") {
        val jwt = makeJwt("BAD-KID", "tester2", keyPair.private)
        val jwtUser = service.extractUsername(jwt)
        jwtUser shouldBe null
      }

      it("fails if incorrect key used") {
        val jwt = makeJwt(kid, "tester3", generateKeyPair().private)
        val jwtUser = service.extractUsername(jwt)
        jwtUser shouldBe null
      }

      it("doesn't crash on JWKS loading error") {
        val badUri = URI.create("http://localhost:98765/").toURL()
        val badService = JwksOboService(badUri, "subject")

        badService.loadJwks()

        badService.keyCount() shouldBe 0
        badService.keyStatus() shouldBe JwksOboService.KeyStatus.FAILED
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
    .claim("subject", username)
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

  return jwksFile.toURI().toURL()
}
