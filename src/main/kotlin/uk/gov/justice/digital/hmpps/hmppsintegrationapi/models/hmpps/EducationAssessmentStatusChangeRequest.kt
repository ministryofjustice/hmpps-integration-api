package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.net.URL
import java.time.LocalDate

/**
 * A request object describing a change of status to a person's Education Assessments.
 */
data class EducationAssessmentStatusChangeRequest(
  @Schema(description = "The current status of the Assessments", example = "COMPLETE")
  val status: EducationAssessmentStatus,
  @Schema(description = "The ISO-8601 formatted date that the status changed.", example = "2025-04-17")
  val statusChangeDate: LocalDate,
  @Schema(description = "The URL of where to get the person's current Education Assessments", example = "https://example.com/sequation-virtual-campus2-api/learnerAssessments/v2/A1234AB")
  val detailUrl: URL,
)

enum class EducationAssessmentStatus {
  ALL_RELEVANT_ASSESSMENTS_COMPLETE,
}
