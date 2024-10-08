package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class SentenceDate(
  @Schema(description = "Effective sentence end date.", example = "2023-03-01")
  val effectiveEndDate: LocalDate? = null,
  @Schema(description = "date on which sentence expired (as calculated by NOMIS).", example = "2023-03-01")
  val expiryCalculatedDate: LocalDate? = null,
  @Schema(description = "date on which sentence expires.", example = "2023-03-01")
  val expiryDate: LocalDate? = null,
  @Schema(description = "date on which sentence expires (override).", example = "2023-03-01")
  val expiryOverrideDate: LocalDate? = null,
  @Schema(description = "Sentence start date.", example = "2023-03-01")
  val startDate: LocalDate? = null,
)
