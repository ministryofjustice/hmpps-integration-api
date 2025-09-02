package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

data class ConfigAuthorisation(
  val endpoints: List<String>,
  val filters: RoleFilters?,
)
