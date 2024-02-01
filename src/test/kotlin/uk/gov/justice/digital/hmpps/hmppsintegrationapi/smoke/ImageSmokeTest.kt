package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class ImageSmokeTest : DescribeSpec({
  val httpClient = IntegrationAPIHttpClient()

  it("returns an image from NOMIS") {
    val id = 2461788

    val response = httpClient.performAuthorised("v1/images/$id")

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.headers().map()["content-type"]?.first().shouldBe("image/jpeg")
  }
},)
