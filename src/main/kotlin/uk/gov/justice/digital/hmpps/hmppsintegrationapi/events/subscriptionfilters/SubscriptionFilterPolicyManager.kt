package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.AuthorisationConfigReader
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager

@Component
class SubscriptionFilterPolicyManager(
  private val fileManager: FileManager? = null,
) {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  val objectMapper: ObjectMapper =
    ObjectMapper()
      .registerKotlinModule()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDefaultPrettyPrinter(
        object : DefaultPrettyPrinter() {
          override fun createInstance(): DefaultPrettyPrinter {
            val prettyPrinter = this
            prettyPrinter.indentArraysWith(DefaultIndenter())
            return prettyPrinter
          }

          override fun writeObjectFieldValueSeparator(g: JsonGenerator) {
            g.writeRaw(": ")
          }
        },
      )

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
          writePolicyFile(env.key, consumer.key, policy, updateType)
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
    val authorisationConfigReader = AuthorisationConfigReader(fileManager!!)
    val authorisation = authorisationConfigReader.read(environment)
    val policyMap = mutableMapOf<String, Pair<FilterPolicy, Boolean>>()

    authorisation.consumersWithQueue().forEach { consumer ->
      logger.info("Checking $environment filter policy file for for $consumer")
      val existingFilterPolicy = readFile(environment, consumer)
      val consumerEvents = authorisation.events(consumer)
      val prisonIds = authorisation.allFilters(consumer)?.prisons
      val filterPolicy = FilterPolicy(consumerEvents, prisonIds)
      val isNewFile = existingFilterPolicy == null
      if (isNewFile || filterPolicy != existingFilterPolicy) {
        policyMap[consumer] = Pair(FilterPolicy(consumerEvents, prisonIds), isNewFile)
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
    val authorisationConfigReader = AuthorisationConfigReader(fileManager!!)
    val authorisation = authorisationConfigReader.read(environment)
    authorisation.consumersWithoutQueue().forEach { consumer ->
      val existingFilterPolicies = readFile(environment, consumer)
      if (existingFilterPolicies != null) {
        logger.info("Deleting $environment filter policy file for $consumer")
        deletePolicyFile(environment, consumer)
      }
    }
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
    fileManager?.write(path, "$content\n")
  }

  /**
   * Function that gets the absolute path to the correct policy folder for a given environment
   *
   * @param environment The environment (e.g dev/preprod/prod])
   */
  fun checkPropertyFolderPath(environment: String): String {
    val resourcesFolder = fileManager?.getResourcesFolderPath()
    val path = "$resourcesFolder/$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment"
    fileManager?.checkOrCreateDirectory(path)
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
    val resourcesFolder = fileManager?.getResourcesFolderPath()
    val path = "$resourcesFolder/$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX"
    fileManager?.delete(path)
  }

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
    val fileContents = fileManager?.readFileContentsFromResourcesFolder("$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX")
    return if (fileContents != null) {
      objectMapper.readValue(fileContents, FilterPolicy::class.java)
    } else {
      null
    }
  }

  /**
   * Function that reads a filter policy file from the class path
   *
   * @param environment The environment (e.g dev/preprod/prod])
   * @param consumer The consumers name
   * @return An optional FilterPolicies object for the consumer and environment
   */
  fun readPolicyFromClasspath(
    environment: String,
    consumer: String,
  ): FilterPolicy? {
    val policyFile = ClassPathResource("$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX")
    return if (policyFile.exists()) {
      readPolicyValueFromString(policyFile.file.readText())
    } else {
      null
    }
  }

  /**
   * Writes a filter policy to a string
   */
  fun writePolicyValueAsString(filterPolicy: FilterPolicy): String = objectMapper.writeValueAsString(filterPolicy)

  /**
   * Reads from a string to a Filter policy
   */
  fun readPolicyValueFromString(filterPolicy: String): FilterPolicy = objectMapper.readValue(filterPolicy, FilterPolicy::class.java)
}
