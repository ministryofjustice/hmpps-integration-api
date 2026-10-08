package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CPRCodeDescription(
  val code: String? = null,
  val description: String? = null,
)
