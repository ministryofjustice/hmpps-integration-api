@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag

import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException

class FeatureFlagAspectTest {
  private var proceedingJoinPoint: ProceedingJoinPoint =
    mock(ProceedingJoinPoint::class.java).apply {
      `when`(proceed()).thenReturn(Unit)
    }
  private val featureFlagConfig: FeatureFlagConfig =
    FeatureFlagConfig(
      mapOf(
        "use-arns-endpoints" to true,
        "use-image-endpoints" to false,
        "use-education-assessments-endpoints" to false,
        "use-physical-characteristics-endpoints" to false,
        "use-residential-hierarchy-endpoints" to false,
        "use-residential-details-endpoints" to false,
        "replace-probation-search" to false,
        "use-capacity-endpoint" to false,
        "use-personal-care-needs-endpoints" to false,
        "use-languages-endpoints" to false
      ),
    )
  private val featureFlagAspect: FeatureFlagAspect =
    FeatureFlagAspect(
      featureFlagConfig = featureFlagConfig,
    )

  @Test
  fun `test feature flag enabled then proceed`() {
    featureFlagAspect.checkFeatureFlag(proceedingJoinPoint, FeatureFlag(FeatureFlagConfig.USE_ARNS_ENDPOINTS))
    verify(proceedingJoinPoint, times(1)).proceed()
    verifyNoMoreInteractions(proceedingJoinPoint)
  }

  @Test
  fun `test feature flag disabled then throw feature flag not enabled exception`() {
    assertThrows<FeatureNotEnabledException> {
      featureFlagAspect.checkFeatureFlag(proceedingJoinPoint, FeatureFlag(FeatureFlagConfig.USE_PHYSICAL_CHARACTERISTICS_ENDPOINTS))
    }
    verify(proceedingJoinPoint, times(0)).proceed()
    verifyNoMoreInteractions(proceedingJoinPoint)
  }
}
