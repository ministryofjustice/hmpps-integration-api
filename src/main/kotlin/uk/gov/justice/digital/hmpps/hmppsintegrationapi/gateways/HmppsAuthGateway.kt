package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.apache.tomcat.util.json.JSONParser
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.AuthenticationFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials
import java.lang.RuntimeException


// Provides a place for all interaction with the auth gateway
@Component
class HmppsAuthGateway(val hmppsAuthClient: WebClient) : IAuthGateway {
  override fun authenticate(credentials: Credentials): String {
    val encodedCredentials = credentials.toBase64()

    return try {
      val response = hmppsAuthClient
        .post()
        .uri("/auth/oauth/token?grant_type=client_credentials")
        .header("Authorization", "Basic $encodedCredentials")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()

      JSONParser(response).parseObject()["access_token"].toString()
    } catch (exception: WebClientRequestException) {
      throw AuthenticationFailedException("Connection to ${exception.uri.authority} failed for NOMIS.")
    } catch (exception: WebClientResponseException.ServiceUnavailable) {
      throw AuthenticationFailedException("${exception.request?.uri?.authority} is unavailable for NOMIS.")
    } catch (exception: WebClientResponseException.Unauthorized) {
      throw AuthenticationFailedException("Invalid credentials used for NOMIS.")
    }
  }
}
