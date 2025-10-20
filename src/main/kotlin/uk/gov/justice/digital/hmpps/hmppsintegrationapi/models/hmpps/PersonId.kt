package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

interface PersonId {
  val id: String
}

data class PersonInPrisonId(
  override val id: String,
  val prisonId: String? = null,
) : PersonId

data class PersonOnProbationId(
  override val id: String,
) : PersonId
