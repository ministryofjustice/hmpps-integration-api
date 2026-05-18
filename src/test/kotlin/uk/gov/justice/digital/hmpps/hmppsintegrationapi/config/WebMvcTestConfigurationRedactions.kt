package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.mockito.Mockito
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuthorisationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@TestConfiguration
class WebMvcTestConfigurationRedactions {
  @Bean
  @ConditionalOnMissingBean
  fun accessFor(): GetCaseAccess = Mockito.mock(GetCaseAccess::class.java)

  @Bean
  @ConditionalOnMissingBean
  fun telemetryService(): TelemetryService = Mockito.mock(TelemetryService::class.java)

  @Bean
  @ConditionalOnMissingBean
  fun config(): AuthorisationConfig = AuthorisationConfig(roles = mapOf("full-access" to testRoleWithLaoRedactions))

  @Bean
  @ConditionalOnMissingBean
  fun authorisationService(): AuthorisationService = AuthorisationService(config())
}
