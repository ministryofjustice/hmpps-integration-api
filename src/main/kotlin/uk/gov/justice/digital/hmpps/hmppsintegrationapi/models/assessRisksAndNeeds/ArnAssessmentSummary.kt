package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import java.time.LocalDateTime

data class ArnAssessmentSummary(
  val assessments: List<ArnAssessmentSummaryItem>? = null,
) {
  fun toAssessmentSummary(): AssessmentSummary {
    val latestAssessment =
      this.assessments
        ?.filter { it.dateCompleted != null }
        ?.maxByOrNull { it.dateCompleted!! }
    return AssessmentSummary(
      latestAssessment?.initiationDate,
      latestAssessment?.dateCompleted,
      latestAssessment?.assessmentType,
      latestAssessment?.assessmentStatus,
      latestAssessment?.assessorName,
      latestAssessment?.countersignerName,
    )
  }
}

data class ArnAssessmentSummaryItem(
  val assessmentId: Int? = null,
  val initiationDate: LocalDateTime? = null,
  val dateCompleted: LocalDateTime? = null,
  val assessmentType: String? = null,
  val assessmentStatus: String? = null,
  val assessorName: String? = null,
  val countersignerName: String? = null,
)
