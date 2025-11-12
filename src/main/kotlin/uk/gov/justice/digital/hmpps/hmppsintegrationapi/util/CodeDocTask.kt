package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.core.io.ClassPathResource
import java.io.File

/**
 * Gradle custom task for generating project documentation from code.
 */
class CodeDocTask {
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      CodeDocTask().generateDocs()
    }
  }

  val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

  /**
   * Generate documentation from code/config.
   */
  fun generateDocs() {
    listUnusedFeatureFlags()
  }

  private fun listUnusedFeatureFlags() {
    println("Feature flags that can be removed from code:-")
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
      println("-  ${it.key} in all ${allConfigs.size} configs")
    }

    val stepSummaryFile = System.getenv("GITHUB_STEP_SUMMARY")
    println("GITHUB_STEP_SUMMARY = ${stepSummaryFile}")
    if (stepSummaryFile != null) {
      val summaryFile = File(stepSummaryFile)
      summaryFile.writeText("Feature flags that can be removed from code: ${allSameFlags.size}\n\n")
    }

  }

  fun listConfigs(): Set<String> =
    File("src/main/resources")
      .walk()
      .filter({ it.name.startsWith("application-") })
      .map({ it.name.replaceFirst("application-", "").replaceFirst(".yml", "") })
      .toSet()

  fun getConfigPath(
    environment: String,
    path: String,
  ): Any = mapper.readTree(ClassPathResource("application-$environment.yml").file).path(path)

  fun getFeatureConfig(environment: String): Map<String, Boolean> {
    val featureConfig = getConfigPath(environment, "feature-flag")
    return mapper.convertValue(featureConfig, object : TypeReference<Map<String, Boolean>>() {})
  }
}
