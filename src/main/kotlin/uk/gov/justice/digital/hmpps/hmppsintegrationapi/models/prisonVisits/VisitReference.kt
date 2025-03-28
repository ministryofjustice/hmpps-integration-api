package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits

import io.swagger.v3.oas.annotations.media.Schema

data class VisitReference(
  @Schema(description = "List of Visit References", example = "[\"dfs-wjs-eqr\", \"abc-123-def\"]")
  val visitReference: String,
)
