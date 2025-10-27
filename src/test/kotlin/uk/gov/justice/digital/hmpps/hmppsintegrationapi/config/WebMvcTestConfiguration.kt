package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.mockito.Mockito
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess

@TestConfiguration
class WebMvcTestConfiguration {
  @Bean
  @ConditionalOnMissingBean
  fun accessFor(): GetCaseAccess = Mockito.mock(GetCaseAccess::class.java)
}
