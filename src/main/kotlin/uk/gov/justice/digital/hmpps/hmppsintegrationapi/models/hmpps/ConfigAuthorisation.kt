package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

data class ConfigAuthorisation(
  val endpoints: List<String>,
  val filters: ConsumerFilters?,
)
