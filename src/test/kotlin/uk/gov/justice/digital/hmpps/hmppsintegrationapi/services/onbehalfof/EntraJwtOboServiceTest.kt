package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Date
import java.util.concurrent.TimeUnit

class EntraJwtOboServiceTest :
  DescribeSpec(
    {
      val keyGenerator = KeyPairGenerator.getInstance("RSA")
      keyGenerator.initialize(2048)
      val key = keyGenerator.generateKeyPair()

      val testJwt = createEntraLikeJwt(key)

      val oboService = EntraJwtOboService()

      // Test does not pass as public key extraction is not in place yet
      xit("decodes and gets unique_name from token") {
        val result = oboService.extractUsername(testJwt)

        result.shouldBe("testName")
      }

      it("decodes and reads values from jwt") {
        val result = oboService.decodeJwt(testJwt, key)

        result?.header?.shouldContain("kid", "testKid")
        result?.payload?.shouldContain("iss", "testIss")
        result?.payload?.shouldContain("appid", "testAppId")
        result?.payload?.shouldContain("unique_name", "testName")
      }

      it("returns null if jwt is empty") {
        val result = oboService.extractUsername("")

        result.shouldBeNull()
      }

      it("returns null if jwt is invalid") {
        val result = oboService.extractUsername("Invalid jwt")

        result.shouldBeNull()
      }
    },
  )

private fun createEntraLikeJwt(key: KeyPair): String {
  // Set expire dates
  val now = Date()
  val expiry = Date(now.time + TimeUnit.HOURS.toMillis(1))

  return Jwts
    .builder()
    .header()
    .keyId("testKid")
    .and()
    .issuer("testIss")
    .subject("testUserSubject")
    .audience()
    .add("testAudience")
    .and()
    .issuedAt(now)
    .expiration(expiry)
    .claim("tid", "testTenant")
    .claim("oid", "12345678-1234-1234-1234-123456789012")
    .claim("appid", "testAppId")
    .claim("name", "testName")
    .claim("unique_name", "testName")
    .claim("scp", "user.read")
    .signWith(key.private, SignatureAlgorithm.RS256)
    .compact()
}
