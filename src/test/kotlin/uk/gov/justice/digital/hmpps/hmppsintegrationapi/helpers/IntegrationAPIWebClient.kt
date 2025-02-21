package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec

class IntegrationAPIWebClient(
  @Autowired val client: WebTestClient,
) {
  private fun setAuthHeader(
    headers: HttpHeaders,
    cn: String = "automated-test-client",
  ) {
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=$cn")
  }

  fun performAuthorised(path: String): ResponseSpec =
    client
      .get()
      .uri(path)
      .headers { headers -> setAuthHeader(headers) }
      .exchange()

  fun performAuthorisedPut(path: String): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return client
      .put()
      .uri(path)
      .headers { headers -> setAuthHeader(headers) }
      .exchange()
  }

  fun performAuthorisedWithCN(
    path: String,
    cn: String,
  ): ResponseSpec =
    client
      .get()
      .uri(path)
      .headers { headers -> setAuthHeader(headers, cn) }
      .exchange()

  fun <T : Any> performAuthorisedPost(
    path: String,
    requestBody: T,
  ): ResponseSpec =
    client
      .post()
      .uri(path)
      .headers { headers ->
        setAuthHeader(headers)
        headers.set("content-type", "application/json")
      }.bodyValue(requestBody)
      .exchange()

  fun <T : Any> performAuthorisedPostWithCN(
    path: String,
    cn: String,
    requestBody: T,
  ): ResponseSpec =
    client
      .post()
      .uri(path)
      .headers { headers ->
        setAuthHeader(headers, cn)
        headers.set("content-type", "application/json")
      }.bodyValue(requestBody)
      .exchange()
}
