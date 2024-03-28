package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class InfoPageTest : DescribeSpec({
  val httpClient = IntegrationAPIHttpClient()

  it("Info page is accessible") {
    val response = httpClient.performAuthorised("info")

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("build.name", "hmpps-integration-api")
  }
})
