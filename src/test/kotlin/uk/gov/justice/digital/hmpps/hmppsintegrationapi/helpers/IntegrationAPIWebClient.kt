package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec

class IntegrationAPIWebClient(
  @Autowired val client: WebTestClient,
) {
  fun performAuthorised(path: String): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return client
      .get()
      .uri(path)
      .header("subject-distinguished-name", subjectDistinguishedName)
      .exchange()
  }

  fun performAuthorisedPut(path: String): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return client
      .put()
      .uri(path)
      .header("subject-distinguished-name", subjectDistinguishedName)
      .exchange()
  }

  fun performAuthorisedWithCN(
    path: String,
    cn: String,
  ): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=$cn"
    return client
      .get()
      .uri(path)
      .header("subject-distinguished-name", subjectDistinguishedName)
      .exchange()
  }

  fun <T : Any> performAuthorisedPost(
    path: String,
    requestBody: T,
  ): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client"
    return client
      .post()
      .uri(path)
      .header("subject-distinguished-name", subjectDistinguishedName)
      .header("content-type", "application/json")
      .bodyValue(requestBody)
      .exchange()
  }

  fun <T : Any> performAuthorisedPostWithCN(
    path: String,
    cn: String,
    requestBody: T,
  ): ResponseSpec {
    val subjectDistinguishedName = "C=GB,ST=London,L=London,O=Home Office,CN=$cn"
    return client
      .post()
      .uri(path)
      .header("subject-distinguished-name", subjectDistinguishedName)
      .header("content-type", "application/json")
      .bodyValue(requestBody)
      .exchange()
  }
}
