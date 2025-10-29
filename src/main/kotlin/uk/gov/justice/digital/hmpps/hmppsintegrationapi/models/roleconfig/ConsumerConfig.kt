package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerConfig(
  val notes: String?,
  val include: List<String>?,
  val filters: ConsumerFilters?,
  val roles: List<String>?,
)
