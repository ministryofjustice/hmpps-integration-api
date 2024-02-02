package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnore

data class Licence(
  @JsonIgnore
  val id: String,
  @JsonIgnore
  val offenderNumber: String? = null,
  val status: String? = null,
  val typeCode: String? = null,
  val createdDate: String? = null,
  val approvedDate: String? = null,
  val updatedDate: String? = null,
  var conditions: List<LicenceCondition> = emptyList(),
)
