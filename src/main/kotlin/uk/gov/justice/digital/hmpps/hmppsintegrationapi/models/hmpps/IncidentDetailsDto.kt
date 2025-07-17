package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class IncidentDetailsDto(
  @Schema(description = "Date and time the incident occurred", example = "2010-10-12T10:00:00")
  val dateTimeOfIncident: String? = null,
)
