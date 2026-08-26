package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import io.swagger.v3.oas.annotations.media.Schema

class PrisonerMovementsResponse(
  @Schema(example = "Normal Transfer")
  val transferReason: String? = null,
  @Schema(example = "NOTR")
  val movementCode: String? = null,
  @Schema(example = "Moorland (HMP & YOI)")
  val toAgencyDescription: String? = null,
  @Schema(example = "Millsike (HMP)")
  val receivedFromDescription: String? = null,
  @Schema(example = "Millsike (HMP)")
  val establishmentName: String? = null,
  @Schema(example = "2026-07-14T12:41:05")
  val movementDateTime: String? = null,
  @Schema(example = "Moorland (HMP & YOI)")
  val courtName: String? = null,
  @Schema(example = "2026-07-14T12:41:05")
  val dateOfFirstMovement: String? = null,
)
