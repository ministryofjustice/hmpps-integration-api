package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import java.time.LocalDateTime

data class ArnAssessmentSummary(
  val initiationDate: LocalDateTime? = null,
  val completedDate: LocalDateTime? = null,
  val assessmentType: String? = null,
  val status: String? = null,
  val assessorName: String? = null,
  val countersignerName: String? = null,
) {
  fun toAssessmentSummary() =
    AssessmentSummary(
      this.initiationDate,
      this.completedDate,
      this.assessmentType,
      this.status,
      this.assessorName,
      this.countersignerName,
    )
}
