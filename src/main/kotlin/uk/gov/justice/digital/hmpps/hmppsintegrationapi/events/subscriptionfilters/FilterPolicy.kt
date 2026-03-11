package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

data class FilterPolicy(
  val eventType: List<String> = listOf("default"),
)

data class SubscriptionFilterPolicy(
  val filterPolicy: FilterPolicy,
  val subscriptionArn: String,
)

const val SUBSCRIPTION_FILTER_FILE_SUFFIX = "subscription-filter.json"
const val SUBSCRIPTION_FILTER_FOLDER_NAME = "event-filter-policies"
const val SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME = "FilterPolicy"
