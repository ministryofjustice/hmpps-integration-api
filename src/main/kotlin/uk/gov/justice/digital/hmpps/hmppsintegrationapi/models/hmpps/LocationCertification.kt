package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class LocationCertification(
  @Schema(description = "Indicates that this location is certified for use as a residential location", example = "true")
  val certified: Boolean,
  @Schema(description = "Indicates the capacity of the certified location (cell)", example = "1")
  val capacityOfCertifiedCell: Int,
)
