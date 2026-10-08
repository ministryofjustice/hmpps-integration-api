package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CPRName(
  val firstName: String? = null,
  val middleNames: String? = null,
  val lastName: String? = null,
  val dateOfBirth: String? = null,
  val aliases: List<CPRAlias> = emptyList(),
)
