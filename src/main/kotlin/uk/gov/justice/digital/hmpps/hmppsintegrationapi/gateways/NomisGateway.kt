package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway(val prisonApiClient: WebClient, val hmppsAuthClient: WebClient) {
  private lateinit var token: String

  init {
    authenticate()
  }

  fun authenticate(): Boolean {
    try {
      val response = hmppsAuthClient
        .post()
        .uri("/auth/oauth/token?grant_type=client_credentials")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()

      token = JSONParser(response).parseObject()["access_token"].toString()
    } catch (exception: Exception) {
      System.err.println("Error: Unable to connect to HMPPS Auth service. [${exception.message}]")
      return false;
    }
    return true;
  }

  fun getPerson(id: String): Person? {
    return if (authenticate()) {
      prisonApiClient
        .get()
        .uri("/api/offenders/$id")
        .header("Authorization", "Bearer $token")
        .retrieve()
        .bodyToMono(Person::class.java)
        .block()
    }
    else
    {
      println("Not authenticated")
      null
    }
  }
}
