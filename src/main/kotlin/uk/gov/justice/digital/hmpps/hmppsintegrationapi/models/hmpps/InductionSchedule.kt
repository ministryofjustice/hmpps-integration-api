package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class InductionSchedule(
  @Schema(
    description = "An ISO-8601 date representing when the Induction should be completed by.",
    example = "2023-09-01",
  )
  val deadlineDate: LocalDate? = null,
  @Schema(
    description = "The current status of the Induction Schedule",
    example = "SCHEDULED",
  )
  val scheduleStatus: String? = null,
  @Schema(
    description = "The Induction Schedule rule used to determine deadline date.",
    example = "NEW_PRISON_ADMISSION",
  )
  val scheduleCalculationRule: String? = null,
  @Schema(
    description = "The prison number or NOMS number of the person.",
    example = "A1234BC",
  )
  val prisonNumber: String? = null,
)
