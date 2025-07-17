package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class HearingDto(
  @Schema(description = "Date and time the hearing will take place", example = "2010-10-12T10:00:00")
  val dateTimeOfHearing: String? = null,
  @Schema(description = "oic hearing type", example = "oic hearing type")
  val oicHearingType: String? = null,
  @Schema(description = "Hearing outcome")
  val outcome: HearingOutcomeDto? = null,
)
