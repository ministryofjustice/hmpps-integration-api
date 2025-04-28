package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ResidentialDetails(
  @Schema(description = "The top level type of locations", example = "Wings")
  val topLevelLocationType: String,
  @Schema(description = "The description of the type of sub locations most common", examples = ["Wings", "Landings", "Spurs", "Cells"])
  val subLocationName: String?,
  @Schema(description = "The current parent location (e.g Wing or Landing) details")
  val parentLocation: Location?,
  @Schema(description = "All residential locations under this parent")
  val subLocations: List<Location>,
)
