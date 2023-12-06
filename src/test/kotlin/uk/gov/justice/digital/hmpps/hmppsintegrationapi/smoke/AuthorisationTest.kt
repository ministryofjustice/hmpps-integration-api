package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class AuthorisationTest : DescribeSpec({
  val httpClient = IntegrationAPIHttpClient()

  describe("Access Allowed") {
    it("when the consumer exists and paths match") {
      val response = httpClient.performAuthorised("info")

      response.statusCode().shouldBe(HttpStatus.OK.value())
    }
  }

  describe("Access Denied") {
    it("when no Subject Distinguished Name is passed") {
      val response = httpClient.performUnauthorised("info")

      response.statusCode().shouldBe(HttpStatus.FORBIDDEN.value())
    }

    it("when a path was requested that doesn't match the allow list") {
      val response = httpClient.performAuthorised("info/foobar")

      response.statusCode().shouldBe(HttpStatus.FORBIDDEN.value())
    }
  }
},)
