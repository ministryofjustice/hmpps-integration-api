package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.lang.RuntimeException

class NomisAuthenticationFailedException(message: String) : RuntimeException(message)

@Component
class NomisGateway(val prisonApiClient: WebClient, val hmppsAuthClient: WebClient) {
  private lateinit var token: String

  fun authenticate(): Boolean {
    try {
      val response = hmppsAuthClient
        .post()
        .uri("/auth/oauth/token?grant_type=client_credentials")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()

      token = JSONParser(response).parseObject()["access_token"].toString()
    } catch (exception: WebClientRequestException) {
      throw NomisAuthenticationFailedException("Connection to ${exception.uri.authority} failed for NOMIS.")
    } catch (exception: WebClientResponseException.ServiceUnavailable) {
      throw NomisAuthenticationFailedException("${exception.request?.uri?.authority} is unavailable for NOMIS.")
    }

    return true
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
    } else {
      println("Not authenticated")
      null
    }
  }
}
