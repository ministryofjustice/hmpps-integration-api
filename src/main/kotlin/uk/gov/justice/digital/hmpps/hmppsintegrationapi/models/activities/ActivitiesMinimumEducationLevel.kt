package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities

data class ActivitiesMinimumEducationLevel(
  val id: Long,
  val educationLevelCode: String,
  val educationLevelDescription: String,
  val studyAreaCode: String,
  val studyAreaDescription: String,
)
