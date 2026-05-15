package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class EntraJwtOboServiceTest :
  DescribeSpec(
    {

      // Test jwt, decoded as follows
      //      {
      //        "header": {
      //          "alg":"HS256"
      //          "typ":"JWT"
      //          "kid":"testKid"
      //        }
      //        "payload":{
      //          "sub":"1234567890"
      //          "name":"John Doe"
      //          "admin":true
      //          "iat":1516239022
      //          "aud":"testAud"
      //          "iss":"testIss"
      //          "nbf":1778083253
      //          "exp":1978087263
      //          "appid":"testId"
      //          "unique_name":"testName"
      //        }
      //      }
      val testJwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6InRlc3RLaWQifQ.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiYXVkIjoidGVzdEF1ZCIsImlzcyI6InRlc3RJc3MiLCJuYmYiOjE3NzgwODMyNTMsImV4cCI6MTk3ODA4NzI2MywiYXBwaWQiOiJ0ZXN0SWQiLCJ1bmlxdWVfbmFtZSI6InRlc3ROYW1lIn0.bppq2M56ruzzxwfEWK408np-22hAJ2vwrHlbHGuWDq0"

      val oboService = EntraJwtOboService()

      it("decodes and gets unique_name from token") {
        val result = oboService.extractUsername(testJwt)

        result.shouldBe("testName")
      }

      it("decodes and reads values from jwt") {
        val result = oboService.decodeJwt(testJwt)

        result?.header?.shouldContain("kid", "testKid")
        result?.payload?.shouldContain("aud", "testAud")
        result?.payload?.shouldContain("iss", "testIss")
        result?.payload?.shouldContain("nbf", 1778083253)
        result?.payload?.shouldContain("exp", 1978087263)
        result?.payload?.shouldContain("appid", "testId")
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
