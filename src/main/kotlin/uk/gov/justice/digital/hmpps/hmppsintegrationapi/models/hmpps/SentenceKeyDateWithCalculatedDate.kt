package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class SentenceKeyDateWithCalculatedDate(
  @Schema(description = "release date for offender", example = "2023-03-01")
  val date: LocalDate? = null,
  @Schema(description = "release override date for offender", example = "2023-03-01")
  val overrideDate: LocalDate? = null,
  @Schema(description = "release calculated date for offender", example = "2023-03-01")
  val calculatedDate: LocalDate? = null,
)
