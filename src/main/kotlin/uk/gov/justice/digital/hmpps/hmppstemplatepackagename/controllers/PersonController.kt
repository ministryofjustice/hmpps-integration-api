// curl -X POST "http://oauth-server:8080/auth/oauth/token?grant_type=client_credentials"
// -H 'Content-Type: application/json'
// -H "Authorization: Basic ZGVsaXVzOmNsaWVudHNlY3JldA=="

package uk.gov.justice.digital.hmpps.hmppstemplatepackagename.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@RestController
@RequestMapping("/persons")
class PersonController {
  private fun fetchAuthToken() : String {

    // todo grab from env var, add url query strings right and what the headers
    val tokenUrl = "http://oauth-server:8080/auth/oauth/token?grant_type=client_credentials"

    val builder: WebClient.Builder = WebClient.builder()
    val token = builder.build()
      .post()
      .uri(tokenUrl)
      .header("Authorization", "Basic ZGVsaXVzOmNsaWVudHNlY3JldA==")
      .retrieve()
      .bodyToMono(String::class.java)
      .block()

    print(token)

    return token
  }

  @GetMapping("/all")
  fun getPerson() {
    val token : String = fetchAuthToken()
  }
}