package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class InfoPageTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("Info page is accessible") {
    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/info")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.body().shouldContainJsonKeyValue("build.name", "hmpps-integration-api")
  }
},)
