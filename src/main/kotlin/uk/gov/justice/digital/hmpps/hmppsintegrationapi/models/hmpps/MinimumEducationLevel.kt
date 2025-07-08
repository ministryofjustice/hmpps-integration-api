package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class MinimumEducationLevel(
  val educationLevelCode: String,
  val educationLevelDescription: String,
  val studyAreaCode: String,
  val studyAreaDescription: String,
)
