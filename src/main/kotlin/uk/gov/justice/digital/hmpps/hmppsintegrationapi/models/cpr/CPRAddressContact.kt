package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CPRAddressContact(
  val type: CPRAddressContactType? = null,
  val value: String? = null,
  val extension: String? = null,
)
