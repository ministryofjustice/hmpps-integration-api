package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MinimumEducationLevel

data class ActivitiesMinimumEducationLevel(
  val id: Long,
  val educationLevelCode: String,
  val educationLevelDescription: String,
  val studyAreaCode: String,
  val studyAreaDescription: String,
) {
  fun toMinimumEducationLevel() =
    MinimumEducationLevel(
      educationLevelCode = this.educationLevelCode,
      educationLevelDescription = this.educationLevelDescription,
      studyAreaCode = this.studyAreaCode,
      studyAreaDescription = this.studyAreaDescription,
    )
}
