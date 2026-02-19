package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
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
    deprecated = true,
  )
  val scoreLevel: String? = null,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val score: Int? = null,
)
