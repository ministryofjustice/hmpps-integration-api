package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PrisonerContact(
  val contact: Contact,
  val relationship: PrisonerContactRelationship,
)
