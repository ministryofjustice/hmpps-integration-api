package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class OffenceRuleDto(
  @Schema(description = "The paragraph number relating to the offence rule they have been alleged to have broken", example = "25(a)")
  val paragraphNumber: String? = null,
  @Schema(description = "The name relating to the paragraph description", example = "Committed an assault")
  val paragraphDescription: String? = null,
)
