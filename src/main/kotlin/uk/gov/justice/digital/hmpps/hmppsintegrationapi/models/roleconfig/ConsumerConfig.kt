package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig.OboConfig

data class ConsumerConfig(
  val include: List<String>? = null,
  val filters: ConsumerFilters? = null,
  val roles: List<String>? = null,
  val notes: String? = null,
  val queueName: String? = null,
  val oboConfig: OboConfig? = null,
) {
  fun permissions(): List<String>? = include
}
