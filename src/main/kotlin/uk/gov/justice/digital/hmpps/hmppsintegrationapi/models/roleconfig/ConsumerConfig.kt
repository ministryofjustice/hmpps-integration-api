package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerConfig(
  val include: List<String>? = null,
  val filters: ConsumerFilters? = null,
  val roles: List<String>? = null,
  val notes: String? = null,
) {
  fun permissions(): List<String>? = include
}
