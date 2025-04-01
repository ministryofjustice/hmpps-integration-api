package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Min

data class Visitor(
  @Schema(description = "Person ID (nomis) of the visitor", required = true)
  @field:Min(value = 1, message = "Nomis person ID must be positive")
  val nomisPersonId: Long,
  @Schema(description = "true if visitor is the contact for the visit otherwise false", required = false)
  val visitContact: Boolean?,
)
