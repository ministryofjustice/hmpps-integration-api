package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Court(
  @Schema(example = "ACCRYC")
  val courtId: String? = null,
  @Schema(example = "Accrington Youth Court")
  val courtName: String? = null,
  @Schema(example = "Accrington Youth Court")
  val courtDescription: String? = null,
  val type: CourtType? = null,
  @Schema(example = "ture")
  val active: Boolean? = null,
  @Schema(example = "faa91bb2-19cb-384b-bcc1-06d31d12cc67")
  val cpCourtUuid: String? = null,
)

data class CourtType(
  @Schema(example = "COU")
  val courtType: String? = null,
  @Schema(example = "County Court/County Divorce Ct")
  val courtName: String? = null,
)
