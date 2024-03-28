package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class IntegrationAPIHttpClient(
  val httpClient: HttpClient = HttpClient.newBuilder().build(),
  val baseUrl: String = "http://localhost:8080",
) {
  fun performAuthorised(path: String): HttpResponse<String> {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"

    val httpRequest =
      HttpRequest
        .newBuilder()
        .headers("subject-distinguished-name", subjectDistinguishedName)

    return httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/$path")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )
  }

  fun performUnauthorised(path: String): HttpResponse<String> {
    val httpRequest = HttpRequest.newBuilder()
    return httpClient.send(
      httpRequest.uri(URI.create("$baseUrl/$path")).build(),
      HttpResponse.BodyHandlers.ofString(),
    )
  }
}
