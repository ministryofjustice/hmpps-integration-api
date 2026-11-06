package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonApiPssDetail(
  @Schema(example = "OFFENDER_DETAILS_REQUEST")
  val type: String? = null,
  @Schema(example = "21")
  val id: Int? = null,
  @Schema(example = "A1417AE")
  val noms_id: String? = null,
  @Schema(example = "BMI")
  val prison_id: String? = null,
  @Schema(example = "2016-10-21 14:55:06.284")
  val timestamp: String? = null,
  val offender_details_request: Map<String, Any>? = null,
)
