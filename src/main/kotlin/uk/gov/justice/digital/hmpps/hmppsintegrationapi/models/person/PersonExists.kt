package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.person

data class PersonExists(
  val crn: String,
  val existsInDelius: Boolean,
)
