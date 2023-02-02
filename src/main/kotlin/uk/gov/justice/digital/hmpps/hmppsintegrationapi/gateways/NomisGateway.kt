package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Credentials
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Person

@Component
class NomisGateway(@Value("\${services.prison-api.base-url}") baseUrl: String) {
  private val webClient: WebClient = WebClient.builder().baseUrl(baseUrl).build()

  @Autowired
  lateinit var hmppsAuthGateway: HmppsAuthGateway

  @Value("\${hmpps-auth.username}")
  private lateinit var username: String

  @Value("\${hmpps-auth.password}")
  private lateinit var password: String

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.getClientToken(Credentials(username, password))

    return webClient
      .get()
      .uri("/api/offenders/$id")
      .header("Authorization", "Bearer $token")
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
