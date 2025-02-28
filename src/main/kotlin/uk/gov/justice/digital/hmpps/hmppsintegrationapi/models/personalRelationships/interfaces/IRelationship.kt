package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces

interface IRelationship {
  val relationshipType: String?
  val relationshipTypeDescription: String?
  val relationshipToPrisoner: String?
  val relationshipToPrisonerDescription: String?
}
