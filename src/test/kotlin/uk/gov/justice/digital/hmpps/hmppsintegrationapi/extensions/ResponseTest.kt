package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

class ResponseTest :
  DescribeSpec({
    it("should be handle error responses") {
      val errors =
        listOf(
          WebClientResponseException(400, "Wrong", null, null, null),
          WebClientResponseException(403, "Nope", null, null, null),
          WebClientResponseException(404, "Lost it", null, null, null),
          WebClientResponseException(502, "Bust", null, null, null),
          RuntimeException("Unknown"),
        )

      val response = Response.error(UpstreamApi.TEST, errors, "Hello, world!")

      response shouldNotBe null
      response.data shouldBe "Hello, world!"
      response.errors.size shouldBe 5
      response.errors[0].description shouldBe "400 Wrong"
      response.errors[0].causedBy shouldBe UpstreamApi.TEST
    }
  })
