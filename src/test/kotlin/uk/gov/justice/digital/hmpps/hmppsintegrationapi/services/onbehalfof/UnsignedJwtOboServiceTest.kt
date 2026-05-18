package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.jsonwebtoken.Jwts
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.util.Date

class UnsignedJwtOboServiceTest :
  DescribeSpec(
    {
      val testJwt = createUnassignedJwy()

      val oboService = UnsignedJwtOboService()

      it("decodes and gets unique_name from token") {
        val result = oboService.extractUsername(testJwt)

        result.shouldBe("testName")
      }

      it("decodes and reads values from jwt") {
        val result = oboService.decodeJwt(testJwt)

        result?.payload?.shouldContain("iss", "testIss")
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

private fun createUnassignedJwy(): String {
  val now = Date()

  return Jwts
    .builder()
    .issuer("testIss")
    .subject("testUserSubject")
    .issuedAt(now)
    .claim("name", "testName")
    .claim("unique_name", "testName")
    .compact()
}
