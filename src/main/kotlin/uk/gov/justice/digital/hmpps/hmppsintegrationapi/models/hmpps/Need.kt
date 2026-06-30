package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Need(
  @Schema(description = "The type of need", example = "DRUG_MISUSE")
  val type: String? = null,
  @Schema(description = "Risk of harm")
  val riskOfHarm: Boolean? = null,
  @Schema(description = "Risk of reoffending")
  val riskOfReoffending: Boolean? = null,
  @Schema(description = "Severity of need", example = "null", deprecated = true)
  @Deprecated("No longer populated by upstream. This will always be null")
  val severity: String? = null,
)
