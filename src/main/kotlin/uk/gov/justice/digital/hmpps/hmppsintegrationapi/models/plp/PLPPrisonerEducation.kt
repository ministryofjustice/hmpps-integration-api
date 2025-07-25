package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerEducation
import java.time.LocalDateTime

data class PLPPrisonerEducation(
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: LocalDateTime,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: LocalDateTime,
  val updatedAtPrison: String,
  val reference: String,
  val educationLevel: String,
  val qualifications: List<PLPQualification>,
) {
  fun toPrisonerEducation() =
    PrisonerEducation(
      educationLevel = this.educationLevel,
      qualifications = this.qualifications.map { it.toQualification() },
    )
}
