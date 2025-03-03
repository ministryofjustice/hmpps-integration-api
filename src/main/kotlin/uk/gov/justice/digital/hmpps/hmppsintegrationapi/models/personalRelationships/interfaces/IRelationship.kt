package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces

interface IRelationship {
  val relationshipTypeCode: String?
  val relationshipTypeDescription: String?
  val relationshipToPrisonerCode: String?
  val relationshipToPrisonerDescription: String?
}
