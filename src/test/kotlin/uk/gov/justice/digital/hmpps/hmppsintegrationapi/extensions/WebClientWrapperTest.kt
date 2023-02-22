package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import io.kotest.core.spec.style.DescribeSpec
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

class WebClientWrapperTest : DescribeSpec({
  describe("#makeWebClientRequest") {
    it("makes a get request") {
      val id = "1234"
      val token = "4567"
      val webClient = WebClientWrapper(baseUrl = "http://localhost:4030", uri = "/api/offenders/$id", authToken = token)
      webClient.get<Person>()
    }
  }
})
