package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class GroupReconviction(
  @Schema(
    description = """
      Indicator for risk of group reconviction. Possible values are:
      `LOW`,
      `MEDIUM`,
      `HIGH`,
      `VERY_HIGH`,
      `NOT_APPLICABLE`
    """,
    example = "LOW",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val scoreLevel: String? = null,
)
