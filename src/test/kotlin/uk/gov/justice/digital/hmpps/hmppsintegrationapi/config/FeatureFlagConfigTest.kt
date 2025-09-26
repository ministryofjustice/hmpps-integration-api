package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import kotlin.test.assertFailsWith

class FeatureFlagConfigTest {
  private val flags: FeatureFlagConfig =
    FeatureFlagConfig(
      mapOf(
        "flag-on" to true,
        "flag-off" to false,
      ),
    )

  @Test
  fun `feature flag correctly enabled`() {
    assertTrue(flags.isEnabled("flag-on"))
  }

  @Test
  fun `feature flag correctly disabled`() {
    assertFalse(flags.isEnabled("flag-off"))
  }

  @Test
  fun `feature flag disabled if missing`() {
    assertFalse(flags.isEnabled("flag-missing"))
  }

  @Test
  fun `requires throws exception if disabled`() {
    assertFailsWith<FeatureNotEnabledException> {
      flags.require("flag-off")
    }
  }

  @Test
  fun `requires throws exception if missing`() {
    assertFailsWith<FeatureNotEnabledException> {
      flags.require("flag-missing")
    }
  }

  @Test
  fun `requires is silent if enabled`() {
    flags.require("flag-on")
  }
}
