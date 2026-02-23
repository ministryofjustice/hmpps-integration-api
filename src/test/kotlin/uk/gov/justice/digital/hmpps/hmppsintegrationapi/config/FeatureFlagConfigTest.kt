package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.memberProperties
import kotlin.test.assertFailsWith

class FeatureFlagConfigTest : ConfigTest() {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

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

  @Test
  fun `no flags are enabled in all configs`() {
    val flagCounts = mutableMapOf<String, Int>()

    val allConfigs = listConfigs()
    for (config in allConfigs) {
      for (flag in getFeatureConfig(config)) {
        val countKey = "${flag.key} is ${flag.value}"
        flagCounts[countKey] = flagCounts.getOrDefault(countKey, 0) + 1
      }
    }

    val allSameFlags = flagCounts.filter { it.value == allConfigs.size }
    allSameFlags.forEach {
      logger.warn("${it.key} is enabled in all ${allConfigs.size} configs")
    }

    // Reduce the threshold in the following assertion over time
    assertTrue(allSameFlags.size <= 14, "Too many flags enabled in all configs: $flagCounts")
  }

  @Test
  fun `no unsued flags`() {
    val flagsInCode =
      FeatureFlagConfig::class
        .companionObject!!
        .memberProperties
        .filter { it.isFinal }
        .map { it.getter.call(FeatureFlagConfig()) }
    val notInCode =
      listConfigs()
        .map { getFeatureConfig(it) }
        .flatMap { it.keys.toList() }
        .filterNot { it in flagsInCode }
        .toSet()
    assertTrue(notInCode.isEmpty(), "The following flags have been left in config: ${notInCode.joinToString(",")}")
  }
}
