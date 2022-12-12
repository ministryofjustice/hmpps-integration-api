// curl -X POST "http://oauth-server:8080/auth/oauth/token?grant_type=client_credentials"
// -H 'Content-Type: application/json'
// -H "Authorization: Basic ZGVsaXVzOmNsaWVudHNlY3JldA=="

package uk.gov.justice.digital.hmpps.hmppstemplatepackagename.controllers

import org.apache.tomcat.util.json.JSONParser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.function.client.WebClient

@RestController
@RequestMapping("/offences")
class OffenceController {
  private fun fetchAuthToken() : String {

    // todo grab from env var, add url query strings right and what the headers
    val tokenUrl = "http://oauth-server:8080/auth/oauth/token?grant_type=client_credentials"

    val builder: WebClient.Builder = WebClient.builder()
    val response = builder.build()
      .post()
      .uri(tokenUrl)
      .header("Authorization", "Basic ZGVsaXVzOmNsaWVudHNlY3JldA==")
      .retrieve()
      .bodyToMono(String::class.java)
      .block()

    print(response)
    val tokenObj = JSONParser(response).parseObject()["access_token"].toString()
    print(tokenObj)
    return tokenObj
  }

  @GetMapping("/all")
  fun getOffences(): String? {
    val token: String = fetchAuthToken()

    val offenceUrl = "http://prison-api:8080/api/offences"
    val builder: WebClient.Builder = WebClient.builder()

    return builder.build()
      .get()
      .uri(offenceUrl)
      .header("Authorization", "Bearer $token")
      .retrieve()
      .bodyToMono(String::class.java)
      .block();
  }
}