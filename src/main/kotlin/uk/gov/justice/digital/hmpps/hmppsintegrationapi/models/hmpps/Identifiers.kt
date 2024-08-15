package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Other unique identifiers for a person.")
data class Identifiers(
  @Schema(description = "A prisoner identifier from NOMIS.", example = "A1234AA")
  val nomisNumber: String? = null,
  @Schema(description = "A Criminal Records Office identifier from National Identification Service (NIS) or National Automated Fingerprint Identification System (NAFIS).", example = "SF80/655108T")
  val croNumber: String? = null,
  @Schema(description = "A Case Reference Number from Delius.", example = "X00001")
  val deliusCrn: String? = null,
)
