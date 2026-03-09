package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FilterPolicyWriter(
  val fileManager: FileManager,
) {
  val objectMapper: ObjectMapper =
    ObjectMapper()
      .registerKotlinModule()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDefaultPrettyPrinter(IndentedArraysPrettyPrettyPrinter())

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * Function that writes the updated FilterPolicy in the relevant environment folder /resources/event-filter-policies folder for an existing filter policy file for a consumer
   *
   * @param environment The environment (e.g dev/preprod/prod])
   * @param consumer The consumers name
   * @param policy The updated policy
   * @param updateType String indicating whether the file is Creating or Updating
   */
  fun writePolicyFile(
    environment: String,
    consumer: String,
    policy: FilterPolicy,
    updateType: String,
  ) {
    val absolutePath = this.checkPropertyFolderPath(environment)
    val path = "$absolutePath/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX"
    logger.info("$updateType $path")
    val content = objectMapper.writeValueAsString(policy)
    fileManager.write(path, "$content\n")
  }

  /**
   * Function that gets the absolute path to the correct policy folder for a given environment
   *
   * @param environment The environment (e.g dev/preprod/prod])
   */
  fun checkPropertyFolderPath(environment: String): String {
    val resourcesFolder = fileManager.getResourcesFolderPath()
    val path = "$resourcesFolder/$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment"
    fileManager.checkOrCreateDirectory(path)
    return path
  }

  /**
   * Function to delete a policy file
   *
   * @param environment The environment (e.g dev/preprod/prod])
   * @param consumer The consumer name
   */
  fun deletePolicyFile(
    environment: String,
    consumer: String,
  ) {
    val resourcesFolder = fileManager.getResourcesFolderPath()
    val path = "$resourcesFolder/$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX"
    fileManager.delete(path)
  }
}
