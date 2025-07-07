package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Qualification
import java.time.LocalDateTime

data class PLPQualification(
  val reference: String,
  val subject: String,
  val level: String,
  val grade: String,
  val createdBy: String,
  val createdAt: LocalDateTime,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedAt: LocalDateTime,
  val updatedAtPrison: String,
) {
  fun toQualification() =
    Qualification(
      subject = this.subject,
      level = this.level,
      grade = this.grade,
    )
}
