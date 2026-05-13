package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager

/**
 * Class to get a Map containing the Authorisation Config for all consumers
 */
class AuthorisationConfigReader(
  val fileManager: FileManager,
) {
  /**
   * Function that reads all consumers authorisation configuration into a map
   *
   * @param environment The environment (e.g dev/preprod/prod)
   * @return Map<String, AuthorisationConfig> A Map of consumers to their associated Authorisation config
   */
  fun read(environment: String): AuthorisationConfig {
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val configFile = fileManager.readFileFromResourcesFolder("application-$environment.yml")
    val config = yamlMapper.readTree(configFile)
    val authConfig = yamlMapper.treeToValue(config.at("/authorisation"), AuthorisationConfig::class.java)
    return authConfig
  }
}
