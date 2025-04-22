package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflags.implementations.FeatureFlagNumberOfChildrenEndpointImpl
import kotlin.test.Test

@Import(FeatureFlagNumberOfChildrenEndpointImplTest.TestConfig::class)
class FeatureFlagNumberOfChildrenEndpointImplTest {
  private val featureFlagConfigMock = Mockito.mock(FeatureFlagConfig::class.java)
  private val featureFlagValidator = FeatureFlagNumberOfChildrenEndpointImpl(featureFlagConfigMock)

  @Test
  fun `validate returns true when feature flag is enabled`() {
    Mockito.`when`(featureFlagConfigMock.useNumberOfChildrenEndpoints).thenReturn(true)
    assertTrue(featureFlagValidator.validate())
  }

  @Test
  fun `validate returns false when feature flag is disabled`() {
    Mockito.`when`(featureFlagConfigMock.useNumberOfChildrenEndpoints).thenReturn(false)
    assertFalse(featureFlagValidator.validate())
  }

  @TestConfiguration
  class TestConfig {
    @Bean
    fun featureFlagConfig(): FeatureFlagConfig = Mockito.mock(FeatureFlagConfig::class.java)
  }
}
