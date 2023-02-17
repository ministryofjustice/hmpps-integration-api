package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.string.shouldContain
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class PersonSmokeTest : DescribeSpec({
  val baseUrl = "http://localhost:8080"
  val httpClient = HttpClient.newBuilder().build()
  val httpRequest = HttpRequest.newBuilder()

  it("returns a person from NOMIS, Prisoner Offender Search and Probation Offender Search") {
    val id = "A1234AL"

    val response = httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/persons/$id")).build(),
      HttpResponse.BodyHandlers.ofString()
    )

    response.body().shouldContain("\"nomis\":{\"firstName\":\"string\",\"lastName\":\"string\"")
  }
})
