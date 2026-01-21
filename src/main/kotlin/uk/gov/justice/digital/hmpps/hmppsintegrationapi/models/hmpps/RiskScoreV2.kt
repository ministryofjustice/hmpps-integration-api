package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RiskScoreV2(
  @Schema(
    description = """
    Indicator for risk score for version 2 outputs. Possible values are:
    `LOW`,
    `MEDIUM`,
    `HIGH`,
    `VERY_HIGH`,
    `NOT_APPLICABLE`
    """,
    example = "VERY_HIGH",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val band: String? = null,
)
