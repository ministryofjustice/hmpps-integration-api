package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnore

data class Licence(
  @JsonIgnore
  val id: String,
  val offenderNumber: String? = null,
  var conditions: List<LicenceCondition> = emptyList(),
)
