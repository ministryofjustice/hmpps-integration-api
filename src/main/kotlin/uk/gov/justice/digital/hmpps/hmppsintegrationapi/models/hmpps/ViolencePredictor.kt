package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

data class ViolencePredictor(
  @Schema(
    description = """
    Indicator for risk of violence. Possible values are:
    `LOW`,
    `MEDIUM`,
    `HIGH`,
    `VERY_HIGH`,
    `NOT_APPLICABLE`
    """,
    example = "MEDIUM",
    allowableValues = ["LOW", "MEDIUM", "HIGH", "VERY_HIGH", "NOT_APPLICABLE"],
    deprecated = true,
  )
  val scoreLevel: String? = null,
  @JsonInclude(JsonInclude.Include.NON_NULL)
  val score: BigDecimal? = null,
)
