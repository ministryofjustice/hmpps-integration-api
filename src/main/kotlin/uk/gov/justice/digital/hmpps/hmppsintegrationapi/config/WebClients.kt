package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClients {
  @Bean("prisonApiClient")
  fun prisonApiClient(@Value("\${services.prison-api.base-url}") prisonApiUrl: String): WebClient {
    return WebClient.builder()
      .baseUrl(prisonApiUrl)
      .build()
  }
}