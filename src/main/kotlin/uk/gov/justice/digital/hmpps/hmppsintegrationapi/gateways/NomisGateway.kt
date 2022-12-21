package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person
import java.lang.RuntimeException
import javax.naming.ConfigurationException

class NomisAuthenticationFailedException(message: String) : RuntimeException(message)

@Component
class NomisGateway(@Value("\${services.prison-api.hmpps-auth.username}") val username: String, @Value("\${services.prison-api.hmpps-auth.password}") val password: String) {
  //Dependency inject WebClients
  @Autowired
  private lateinit var prisonApiClient: WebClient
  @Autowired
  private lateinit var hmppsAuthClient: WebClient

  private lateinit var token: String
  private var credentials = Credentials(username, password)

  fun authenticate(creds: Credentials): Boolean {
    val encodedCreds = creds.toBase64()
    try {
      val response = hmppsAuthClient
        .post()
        .uri("/auth/oauth/token?grant_type=client_credentials")
        .header("Authorization", "Basic $encodedCreds")
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
    return if (authenticate(credentials)) {
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
