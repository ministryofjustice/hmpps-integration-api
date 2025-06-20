package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Schema(
  description =
    """
  Describes the search parameters to use to filter appointments.
  """,
)
data class AppointmentSearchRequest(
  @Schema(
    description =
      """
    The appointment type (INDIVIDUAL or GROUP) to match with the appointment series. Will restrict the search results to
    appointments that are part of a series with the matching type when this search parameter is supplied.
    """,
    example = "INDIVIDUAL",
  )
  val appointmentType: String? = null,
  @field:NotNull(message = "Start date must be supplied")
  @Schema(
    description =
      """
    The start date to match with the appointments. Will restrict the search results to appointments
    that have the matching start date when this search parameter is supplied but no end date is supplied.
    When an end date is also supplied, the search uses a date range and will restrict the search results to
    appointments that have a start date within the date range.
    """,
  )
  @JsonFormat(pattern = "yyyy-MM-dd")
  val startDate: LocalDate,
  @Schema(
    description = """
    When an end date is supplied alongside the start date, the search uses a date range and will restrict the search results to
    appointments that have a start date within the date range.
    """,
  )
  @JsonFormat(pattern = "yyyy-MM-dd")
  val endDate: LocalDate? = null,
  @Schema(
    description =
      """
    The time slot to match with the appointments. Will restrict the search results to appointments that have a start
    time between the times defined by the prison for that time slot when this search parameter is supplied.
    """,
    example = "[\"AM\",\"PM\",\"ED\"]",
  )
  val timeSlots: List<String>? = emptyList(),
  @Schema(
    description =
      """
    The NOMIS reference code to match with the appointments. Will restrict the search results to appointments
    that have the matching category code when this search parameter is supplied.
    """,
    example = "GYMW",
  )
  val categoryCode: String? = null,
  @Schema(
    description =
      """
    The in cell flag value to match with the appointments. Will restrict the search results to appointments
    that have the matching in cell value when this search parameter is supplied.
    """,
    example = "false",
  )
  val inCell: Boolean? = null,
  @Schema(
    description =
      """
    The allocated prisoner or prisoners to match with the appointments. Will restrict the search results to
    appointments that have the at least one of the supplied prisoner numbers attending when this search parameter
    is supplied.
    """,
    example = "[\"A1234BC\"]",
  )
  val prisonerNumbers: List<String>? = null,
) {
  fun toApiConformingMap(): Map<String, Any?> =
    mapOf(
      "appointmentType" to appointmentType,
      "startDate" to startDate.toString(),
      "endDate" to endDate.toString(),
      "timeSlots" to timeSlots,
      "categoryCode" to categoryCode,
      "inCell" to inCell,
      "prisonerNumbers" to prisonerNumbers,
    ).filterValues { it != null }
}
