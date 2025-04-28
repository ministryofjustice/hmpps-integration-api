package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class ResidentialHierarchyItem(
  @Schema(description = "Location ID", example = "2475f250-434a-4257-afe7-b911f1773a4d", required = true)
  val locationId: String,
  @Schema(description = "Location type", example = "CELL", required = true)
  val locationType: String,
  @Schema(description = "Location code", example = "001", required = true)
  val locationCode: String,
  @Schema(description = "Full path of the location within the prison", required = true)
  val fullLocationPath: String,
  @Schema(description = "Alternative description to display for location, (Not Cells)", example = "Wing A")
  val localName: String?,
  @Schema(description = "Current Level within hierarchy, starts at 1, e.g Wing = 1", examples = ["1", "2", "3"], required = true)
  val level: Int,
  @Schema(description = "Sub residential locations")
  val subLocations: List<ResidentialHierarchyItem>?,
)
