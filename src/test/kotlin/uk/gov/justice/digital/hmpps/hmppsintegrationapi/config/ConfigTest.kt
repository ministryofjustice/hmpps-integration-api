package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.core.io.ClassPathResource
import java.io.File

/**
 * Abstract base class for unit tests of configuration files.
 */
abstract class ConfigTest {
  val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

  fun getConfigPath(
    environment: String,
    path: String,
  ): Any = mapper.readTree(ClassPathResource("application-$environment.yml").file).path(path)

  /**
   * Loads the configuration for a specified environment.
   */
  fun getAuthConfig(environment: String): AuthorisationConfig {
    val authConfig = getConfigPath(environment, "authorisation")
    return mapper.convertValue(authConfig, object : TypeReference<AuthorisationConfig>() {})
  }

  fun getFeatureConfig(environment: String): Map<String, Boolean> {
    val featureConfig = getConfigPath(environment, "feature-flag")
    return mapper.convertValue(featureConfig, object : TypeReference<Map<String, Boolean>>() {})
  }

  /**
   * Parses the specified config text as a particular config class.
   */
  inline fun <reified T> parseConfig(config: String): T = mapper.readValue(config, T::class.java)

  /**
   * Returns a list of all the configured environments.
   */
  fun listConfigs(): Set<String> =
    File("src/main/resources")
      .walk()
      .filter({ it.name.startsWith("application-") })
      .map({ it.name.replaceFirst("application-", "").replaceFirst(".yml", "") })
      .toSet()
}
