package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@Configuration
class RestClientConfig {
  @Bean("sanRestClient")
  fun sanRestClient(
    @Value("\${services.san.base-url}") baseUrl: String,
  ): RestApiClient = RestApiClient(UpstreamApi.SAN.name, baseUrl)

  @Bean("rasRestClient")
  fun rasRestClient(
    @Value("\${services.remand-and-sentencing.base-url}") baseUrl: String,
  ): RestApiClient = RestApiClient(UpstreamApi.REMAND_AND_SENTENCING.name, baseUrl)

  @Bean("courtRegisterRestClient")
  fun courtRegisterRestClient(
    @Value("\${services.court-register.base-url}") baseUrl: String,
  ): RestApiClient = RestApiClient(UpstreamApi.COURT_REGISTER.name, baseUrl)
}
