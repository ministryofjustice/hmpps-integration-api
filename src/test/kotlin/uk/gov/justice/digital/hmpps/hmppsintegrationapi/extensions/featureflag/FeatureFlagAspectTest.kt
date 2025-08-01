@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.featureflag

import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
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
        "use-endpoint-1" to true,
        "use-endpoint-2" to false,
      ),
    )
  private val featureFlagAspect: FeatureFlagAspect =
    FeatureFlagAspect(
      featureFlagConfig = featureFlagConfig,
    )

  @Test
  fun `test feature flag enabled then proceed`() {
    featureFlagAspect.checkFeatureFlag(proceedingJoinPoint, FeatureFlag("use-endpoint-1"))
    verify(proceedingJoinPoint, times(1)).proceed()
    verifyNoMoreInteractions(proceedingJoinPoint)
  }

  @Test
  fun `test feature flag disabled then throw feature flag not enabled exception`() {
    assertThrows<FeatureNotEnabledException> {
      featureFlagAspect.checkFeatureFlag(proceedingJoinPoint, FeatureFlag("use-endpoint-2"))
    }
    verify(proceedingJoinPoint, times(0)).proceed()
    verifyNoMoreInteractions(proceedingJoinPoint)
  }
}
