package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nonAssociation

data class NonAssociationsResponse(
  val nonAssociations: NonAssociations?,
  val prisonId: String,
)
