package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.util.Base64

@Component
class NomisGateway {
  private lateinit var token: String

  init {
    val getOAuthUrl = "http://localhost:9090/auth/oauth/token?grant_type=client_credentials"
    val builder: WebClient.Builder = WebClient.builder()

    val encodedBasicAuth = Base64.getEncoder().encodeToString("client:client-secret".toByteArray())

    val response = builder.build()
      .post()
      .uri(getOAuthUrl)
      .header("Authorization", "Basic $encodedBasicAuth")
      .retrieve()
      .bodyToMono(String::class.java)
      .block()

    token = JSONParser(response).parseObject()["access_token"].toString()
  }

  fun getPerson(id: String): Person? {
    val getOffenderUrl = "http://localhost:8081/api/offenders/$id"
    val builder: WebClient.Builder = WebClient.builder()

    return builder.build()
      .get()
      .uri(getOffenderUrl)
      .header("Authorization", "Bearer $token")
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
