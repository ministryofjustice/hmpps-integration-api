package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerConfig(
  val include: List<String>?,
  val filters: ConsumerFilters?,
)
