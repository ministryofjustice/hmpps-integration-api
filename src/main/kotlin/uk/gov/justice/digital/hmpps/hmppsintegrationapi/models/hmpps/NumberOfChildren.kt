package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class NumberOfChildren(
  @Schema(description = "Number of children", example = "2")
  val numberOfChildren: String?,
)
