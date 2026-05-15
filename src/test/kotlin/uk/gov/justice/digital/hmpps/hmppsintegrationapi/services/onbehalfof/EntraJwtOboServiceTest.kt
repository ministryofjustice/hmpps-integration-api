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
      //          "alg":"none"
      //          "kid":"testKid"
      //        }
      //        "payload":{
      //          "sub":"1234567890"
      //          "name":"John Doe"
      //          "admin":true
      //          "iat":1516239022
      //          "aud":"testAud"
      //          "iss":"testIss"
      //          "appid":"testId"
      //          "unique_name":"testName"
      //        }
      //      }
      val testJwt = "eyJhbGciOiJub25lIiwia2lkIjoidGVzdEtpZCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiYXVkIjoidGVzdEF1ZCIsImlzcyI6InRlc3RJc3MiLCJhcHBpZCI6InRlc3RJZCIsInVuaXF1ZV9uYW1lIjoidGVzdE5hbWUifQ."

      val oboService = EntraJwtOboService()

      it("decodes and gets unique_name from token") {
        val result = oboService.extractUsername(testJwt)

        result.shouldBe("testName")
      }

      it("decodes and reads values from jwt") {
        val result = oboService.decodeJwt(testJwt)

        result?.header?.shouldContain("kid", "testKid")
        result?.payload?.shouldContain("iss", "testIss")
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
