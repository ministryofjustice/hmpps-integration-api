package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PrisonerEducation(
  val educationLevel: String,
  val qualifications: List<Qualification>,
)
