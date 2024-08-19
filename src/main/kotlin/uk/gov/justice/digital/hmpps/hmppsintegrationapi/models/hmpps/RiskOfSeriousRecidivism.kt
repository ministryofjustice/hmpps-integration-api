package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class RiskOfSeriousRecidivism(
  @Schema(
    description = """
        Indicator for risk of serious recidivism. Possible values are:
        `LOW`,
        `MEDIUM`,
        `HIGH`,
        `VERY_HIGH`,
        `NOT_APPLICABLE`
      """,
    example = "MEDIUM",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val scoreLevel: String? = null,
)
