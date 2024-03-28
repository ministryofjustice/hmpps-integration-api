package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient

class HealthCheckTest : DescribeSpec({
  val httpClient = IntegrationAPIHttpClient()

  it("Health page is accessible") {
    val response = httpClient.performAuthorised("health")
    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health ping page is accessible") {
    val response = httpClient.performAuthorised("health/ping")

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health readiness page is accessible") {
    val response = httpClient.performAuthorised("health/readiness")
    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health liveness page is accessible") {
    val response = httpClient.performAuthorised("health/liveness")

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }
})
