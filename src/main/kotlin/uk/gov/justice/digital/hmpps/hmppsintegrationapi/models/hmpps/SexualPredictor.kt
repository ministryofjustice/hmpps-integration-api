package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class SexualPredictor(
  @Schema(
    description = """
      Indicator for risk of sexual indecency. Possible values are:
      `LOW`,
      `MEDIUM`,
      `HIGH`,
      `VERY_HIGH`,
      `NOT_APPLICABLE`,
    """,
    example = "MEDIUM",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val indecentScoreLevel: String? = null,
  @Schema(
    description = """
      Indicator for risk of sexual contact. Possible values are:
      `LOW`,
      `MEDIUM`,
      `HIGH`,
      `VERY_HIGH`,
      `NOT_APPLICABLE`,
    """,
    example = "MEDIUM",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val contactScoreLevel: String? = null,
)
