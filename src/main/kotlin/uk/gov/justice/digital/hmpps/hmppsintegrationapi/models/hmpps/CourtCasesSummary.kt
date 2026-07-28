package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class CourtCasesSummary(
  @Schema(description = "The date of first conviction", example = "2022-10-01")
  val dateOfFirstConviction: LocalDate? = null,
)
