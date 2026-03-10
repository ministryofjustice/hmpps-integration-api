package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.AuthorisationConfigReader

class SubscriptionFilterPolicyService(
  val filterPolicyReader: FilterPolicyReader,
  val filterPolicyWriter: FilterPolicyWriter,
  val authorisationConfigReader: AuthorisationConfigReader,
) {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * Function that creates and writes subscription filter policy files for a given list of environments
   * @param environments The list of environments e.g ["dev", "preprod", "prod"]
   */
  fun generatePolicyFiles(environments: List<String>) {
    try {
      val policyMap = mutableMapOf<String, Map<String, Pair<FilterPolicy, Boolean>>>()
      environments.forEach { env ->
        policyMap[env] = generatePoliciesForEnvironment(env)
      }
      writePolicyFiles(policyMap)
      logger.info("Successfully finished generating subscription config files")
    } catch (exception: Exception) {
      logger.error("Failed to generate policies config with error: ${exception.message}")
      throw exception
    }
  }

  /**
   * Function that creates subscription filter policies for each consumer in an environment
   * @param environment The environment (e.g dev/preprod/prod)
   * @return A Map of consumers to Filter Policies in each environment stating if the file is new or updated
   */
  fun generatePoliciesForEnvironment(environment: String): Map<String, Pair<FilterPolicy, Boolean>> = generatePolicies(environment)

  /**
   * Function that writes the subscription filter policy files for each consumer in all environments
   * Uses the filterPolicyWriter to write the files to a folder structure (for each environment) in the resources/event-filter-policies folder
   * @param policies A Map containing all changes for each consumer in each environment
   */
  fun writePolicyFiles(policies: Map<String, Map<String, Pair<FilterPolicy, Boolean>>>) {
    policies.forEach { env ->
      if (env.value.isEmpty()) {
        logger.info("No policy changes identified for ${env.key}")
      } else {
        env.value.forEach { consumer ->
          val policy = consumer.value.first
          val updateType = if (consumer.value.second) "Created" else "Updated"
          filterPolicyWriter.writePolicyFile(env.key, consumer.key, policy, updateType)
        }
      }
      removeExistingWithoutQueue(env.key)
    }
  }

  /**
   * Function that, for each consumer in a specified environment, builds a subscription filter policy.
   * Gets the list of endpoints associated with each consumer and builds a list of events corresponding to those endpoints
   * that the consumer should subscribe to
   *
   * @param environment The environment (e.g dev/preprod/prod)
   */
  fun generatePolicies(environment: String): Map<String, Pair<FilterPolicy, Boolean>> {
    logger.info("Checking policies for $environment")
    val authorisation = authorisationConfigReader.read(environment)
    val policyMap = mutableMapOf<String, Pair<FilterPolicy, Boolean>>()

    authorisation.consumersWithQueue().forEach { consumer ->
      logger.info("Checking $environment filter policy file for for $consumer")
      val existingFilterPolicies = filterPolicyReader.readFile(environment, consumer)
      val consumerEvents = authorisation.events(consumer)
      val isNewFile = existingFilterPolicies == null
      if (isNewFile || existingFilterPolicies.eventType.toSet() != consumerEvents.toSet()) {
        policyMap[consumer] = Pair(FilterPolicy(consumerEvents), isNewFile)
      }
    }
    return policyMap
  }

  /**
   * Function to remove any filter policy files that exist for a consumer without a configured queue
   *
   * @param environment
   */
  fun removeExistingWithoutQueue(environment: String) {
    val authorisation = authorisationConfigReader.read(environment)
    authorisation.consumersWithoutQueue().forEach { consumer ->
      val existingFilterPolicies = filterPolicyReader.readFile(environment, consumer)
      if (existingFilterPolicies != null) {
        logger.info("Deleting $environment filter policy file for $consumer")
        filterPolicyWriter.deletePolicyFile(environment, consumer)
      }
    }
  }
}
