package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager

/**
 * Main method used by the Gradle task 'generateFilterPolicies' to generate subscription filter policy files for dev, preprod and prod
 * Run from the root of the project
 * ./gradlew generateFilterPolicies
 */
object SubscriptionConfigGenerator {
  @JvmStatic
  fun main(args: Array<String>) {
    val generator = SubscriptionFilterPolicyManager(FileManager())
    generator.generatePolicyFiles(listOf("dev", "preprod", "prod"))
  }
}
