package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import java.util.Base64

@Configuration
class WebClients {
  @Bean
  fun prisonApiClient(@Value("\${services.prison-api.base-url}") prisonApiUrl: String): WebClient {
    return WebClient.builder()
      .baseUrl(prisonApiUrl)
      .build()
  }

  @Bean
  fun hmppsAuthClient(
    @Value("\${services.hmpps-auth.base-url}") hmppsAuthUrl: String,
    @Value("\${services.hmpps-auth.client}") client: String,
    @Value("\${services.hmpps-auth.client-secret}") clientSecret: String,
  ): WebClient {
    val encodedBasicAuth = Base64.getEncoder().encodeToString("$client:$clientSecret".toByteArray())

    return WebClient.builder()
      .baseUrl(hmppsAuthUrl)
      .defaultHeader("Authorization", "Basic $encodedBasicAuth")
      .build()
  }
}
