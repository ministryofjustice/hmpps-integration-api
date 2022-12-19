package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway(val prisonApiClient: WebClient, hmppsAuthClient: WebClient) {
  private lateinit var prisonApiToken: String

  init {
    val response = hmppsAuthClient
      .post()
      .uri("/auth/oauth/token?grant_type=client_credentials")
      .retrieve()
      .bodyToMono(String::class.java)
      .block()

    prisonApiToken = JSONParser(response).parseObject()["access_token"].toString()
  }

  fun getPerson(id: String): Person? {
    return prisonApiClient
      .get()
      .uri("/api/offenders/$id")
      .header("Authorization", "Bearer $prisonApiToken")
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
