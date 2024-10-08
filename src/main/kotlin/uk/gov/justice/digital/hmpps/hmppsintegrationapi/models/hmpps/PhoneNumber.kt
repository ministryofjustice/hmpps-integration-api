package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PhoneNumber(
  @Schema(description = "A phone number", example = "079123456789")
  val number: String? = null,
  @Schema(description = "The type of number", example = "TELEPHONE")
  val type: String? = null,
)
