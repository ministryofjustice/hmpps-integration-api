package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

data class FilterPolicy(
  val eventType: List<String> = listOf("default"),
  val prisonId: List<String>? = null,
) {
  fun isDifferentTo(filterPolicy: FilterPolicy): Boolean {
    val thisPolicy = FilterPolicy(this.eventType.sorted(), this.prisonId?.sorted())
    val comparingPolicy = FilterPolicy(filterPolicy.eventType.sorted(), filterPolicy.prisonId?.sorted())
    return thisPolicy != comparingPolicy
  }
}

data class SubscriptionFilterPolicy(
  val filterPolicy: String?,
  val subscriptionArn: String,
)

const val SUBSCRIPTION_FILTER_FILE_SUFFIX = "subscription-filter.json"
const val SUBSCRIPTION_FILTER_FOLDER_NAME = "event-filter-policies"
const val SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME = "FilterPolicy"
