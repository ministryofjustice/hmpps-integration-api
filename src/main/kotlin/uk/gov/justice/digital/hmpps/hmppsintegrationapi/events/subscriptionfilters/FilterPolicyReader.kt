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
   * @param environment The environment
   * @param consumer The consumer
   * @return An optional FilterPolicies object
   */
  fun readFile(
    environment: String,
    consumer: String,
  ): FilterPolicy? {
    val fileContents = fileManager.readFileContentsFromResourcesFolder("event-filter-policies/$environment/$consumer-subscription-filter.json")
    return if (fileContents != null) {
      jsonMapper.readValue(fileContents, FilterPolicy::class.java)
    } else {
      null
    }
  }
}
