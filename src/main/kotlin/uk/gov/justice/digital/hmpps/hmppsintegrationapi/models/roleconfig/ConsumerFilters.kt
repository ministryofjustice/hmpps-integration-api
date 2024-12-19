package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

data class ConsumerFilters(
  val filters: Map<String, List<String>>? = emptyMap(),
)
