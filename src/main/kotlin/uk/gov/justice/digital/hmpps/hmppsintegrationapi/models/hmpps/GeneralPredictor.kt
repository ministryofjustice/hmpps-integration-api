package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

data class GeneralPredictor(
  @Schema(
    description = """
    Indicator for general prediction. Possible values are:
    `LOW`,
    `MEDIUM`,
    `HIGH`,
    `VERY_HIGH`,
    `NOT_APPLICABLE`
    """,
    example = "VERY_HIGH",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
  )
  val scoreLevel: String? = null,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val ogp2Year: Int? = null,
)
