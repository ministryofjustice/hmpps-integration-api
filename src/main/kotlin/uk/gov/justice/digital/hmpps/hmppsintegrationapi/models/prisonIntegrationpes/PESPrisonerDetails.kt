package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes

data class PESPrisonerDetails(
  val prisonerNumber: String? = null,
  val firstName: String,
  val lastName: String,
  val prisonId: String? = null,
  val prisonName: String? = null,
  val cellLocation: String? = null,
)
