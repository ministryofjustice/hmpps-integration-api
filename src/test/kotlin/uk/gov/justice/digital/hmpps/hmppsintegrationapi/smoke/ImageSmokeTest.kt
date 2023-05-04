package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ImageSmokeTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("returns an image from NOMIS") {
    val id = 2461788

    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/v1/images/$id")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )

    response.statusCode().shouldBe(HttpStatus.OK.value())
    response.headers().map()["content-type"]?.first().shouldBe("image/jpeg")
  }
},)
