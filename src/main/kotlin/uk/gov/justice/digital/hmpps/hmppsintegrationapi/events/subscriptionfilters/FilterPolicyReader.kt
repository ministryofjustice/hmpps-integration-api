package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class FilterPolicyReader(
  val fileManager: FileManager,
) {
  val jsonMapper = ObjectMapper().registerKotlinModule()

  /**
   * Function that checks the in relevant environment folder /resources/event-filter-policies folder for an existing filter policy file for a consumer
   * If this exists, then this is read into a FilterPolicies object
   *
   * @param environment The environment (e.g dev/preprod/prod])
   * @param consumer The consumers name
   * @return An optional FilterPolicies object for the consumer and environment
   */
  fun readFile(
    environment: String,
    consumer: String,
  ): FilterPolicy? {
    val fileContents = fileManager.readFileContentsFromResourcesFolder("$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX")
    return if (fileContents != null) {
      jsonMapper.readValue(fileContents, FilterPolicy::class.java)
    } else {
      null
    }
  }
}
