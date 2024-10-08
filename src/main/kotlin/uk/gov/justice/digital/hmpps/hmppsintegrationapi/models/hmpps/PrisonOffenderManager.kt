package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PrisonOffenderManager(
  val forename: String? = null,
  val surname: String? = null,
  val prison: Prison = Prison(),
)
