package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class HealthCheckTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("Health page is accessible") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/health")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health ping page is accessible") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/health/ping")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health readiness page is accessible") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/health/readiness")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }

  it("Health liveness page is accessible") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/health/liveness")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("status", "UP")
  }
},)
