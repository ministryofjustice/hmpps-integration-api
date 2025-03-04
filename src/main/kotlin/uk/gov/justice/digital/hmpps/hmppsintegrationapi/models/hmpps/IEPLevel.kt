package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class IEPLevel(
  @Schema(description = "Incentive Level Code", example = "STD")
  val iepCode: String,
  @Schema(description = "Incentive Level", example = "Standard")
  val iepLevel: String,
)
