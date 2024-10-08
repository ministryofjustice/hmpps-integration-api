package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class Needs(
  @Schema(description = "Needs assessment completion date", example = "2023-09-05T10:15:41")
  val assessedOn: LocalDateTime? = null,
  val identifiedNeeds: List<Need> = emptyList(),
  val notIdentifiedNeeds: List<Need> = emptyList(),
  val unansweredNeeds: List<Need> = emptyList(),
)
