package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

data class FilterPolicy(
  val eventType: List<String> = emptyList(),
)
