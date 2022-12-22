package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway(val prisonApiClient: WebClient, val hmppsAuthGateway: HmppsAuthGateway) {
  @Value("\${services.prison-api.hmpps-auth.username}")
  private lateinit var username: String

  @Value("\${services.prison-api.hmpps-auth.password}")
  private lateinit var password: String

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.authenticate(Credentials(username, password))

    return prisonApiClient
      .get()
      .uri("/api/offenders/$id")
      .header("Authorization", "Bearer $token")
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
