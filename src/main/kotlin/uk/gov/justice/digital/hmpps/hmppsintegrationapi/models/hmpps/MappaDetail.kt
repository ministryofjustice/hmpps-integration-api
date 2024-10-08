package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class MappaDetail(
  @Schema(example = "1")
  val level: Number? = null,
  @Schema(example = "Description of M1")
  val levelDescription: String? = null,
  @Schema(example = "2")
  val category: Number? = null,
  @Schema(example = "Description of M2")
  val categoryDescription: String? = null,
  @Schema(example = "2024-02-07")
  val startDate: String? = null,
  @Schema(example = "2024-02-07")
  val reviewDate: String? = null,
  @Schema(example = "Mappa Detail for X00001")
  val notes: String? = null,
)
