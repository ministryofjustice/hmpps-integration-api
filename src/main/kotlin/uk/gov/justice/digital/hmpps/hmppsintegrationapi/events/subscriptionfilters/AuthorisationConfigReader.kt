package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v2.ConfigController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ConfigAuthorisation

/**
 * Class to get a Map containing the Authorisation Config for all consumers
 */
class AuthorisationConfigReader(
  val fileManager: FileManager,
) {
  /**
   * Function that returns a Map of consumers and their associated Authorisation config
   * This reads from the environments application.yml file and reads the authorisation section into the ConfigAuthorisation class
   *
   * @param environment The environment
   * @return A Map of consumers and their associated Authorisation config
   */
  fun read(environment: String): Map<String, ConfigAuthorisation> {
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    val configFile = fileManager.readFileFromResourcesFolder("application-$environment.yml")
    val config = yamlMapper.readTree(configFile)
    val authConfig = yamlMapper.treeToValue(config.at("/authorisation"), AuthorisationConfig::class.java)
    val authorisation = ConfigController(authConfig).getAuthorisation()
    return authorisation
  }
}
