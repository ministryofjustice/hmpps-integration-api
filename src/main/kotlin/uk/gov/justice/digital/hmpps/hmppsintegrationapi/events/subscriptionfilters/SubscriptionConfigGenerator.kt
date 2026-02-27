package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

class SubscriptionConfigGenerator {
  companion object {
    /**
     * Main method used by the Gradle task 'generateFilterPolicies' to generate subscription filter policy files for dev, preprod and prod
     * Run from the root of the project
     * ./gradlew generateFilterPolicies
     */
    @JvmStatic
    fun main(args: Array<String>) {
      val fileManager = FileManager()
      val generator = SubscriptionFilterPolicyService(FilterPolicyReader(fileManager), FilterPolicyWriter(fileManager), AuthorisationConfigReader(fileManager))
      generator.generatePolicyFiles(listOf("dev", "preprod", "prod"))
    }
  }
}
