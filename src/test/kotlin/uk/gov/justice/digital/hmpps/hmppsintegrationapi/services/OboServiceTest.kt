package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.nulls.shouldBeNull

class OboServiceTest :
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

      val oboService: OboService = OboService()

      it("decodes and reads values from jwt") {
        val result = oboService.decodeJwt(testJwt)

        result?.shouldContain("kid", "testKid")
        result?.shouldContain("aud", "testAud")
        result?.shouldContain("iss", "testIss")
        result?.shouldContain("nbf", 1778083253)
        result?.shouldContain("exp", 1978087263)
        result?.shouldContain("appid", "testId")
        result?.shouldContain("unique_name", "testName")
      }

      it("returns null if jwt is empty") {
        val result = oboService.decodeJwt(null)

        result.shouldBeNull()
      }

      it("returns null if jwt is invalid") {
        val result = oboService.decodeJwt("Invalid jwt")

        result.shouldBeNull()
      }
    },
  )
