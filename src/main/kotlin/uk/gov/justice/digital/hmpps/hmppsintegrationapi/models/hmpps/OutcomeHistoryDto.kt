package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class OutcomeHistoryDto(
  @Schema(description = "Hearing related to adjudication")
  val hearing: HearingDto? = null,
  val outcome: CombinedOutcomeDto? = null,
)
