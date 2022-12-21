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

@Component
class NomisGateway() {
  //Dependency inject WebClients
  @Autowired
  private lateinit var prisonApiClient: WebClient
  @Autowired
  private lateinit var hmppsAuthGateway: IAuthGateway

  //Loaded from contextual config
  @Value("\${services.prison-api.hmpps-auth.username}")
  private lateinit var username: String
  @Value("\${services.prison-api.hmpps-auth.password}")
  private lateinit var password: String

  fun getPerson(id: String): Person? {
    val token = hmppsAuthGateway.authenticate(Credentials(username,password))

    return prisonApiClient
      .get()
      .uri("/api/offenders/$id")
      .header("Authorization", "Bearer $token")
      .retrieve()
      .bodyToMono(Person::class.java)
      .block()
  }
}
