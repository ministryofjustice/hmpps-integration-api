package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactId

data class LinkedPrisoner(
  val prisonerNumber: String,
  val lastName: String,
  val firstName: String,
  val middleNames: String?,
  val relationships: List<Relationship>?,
) {
  fun toPrisonerContactId(): List<PrisonerContactId> = relationships?.map { relationship -> PrisonerContactId(relationship.prisonerContactId) } ?: emptyList()
}

data class Relationship(
  val prisonerContactId: Long?,
  val relationshipType: String?,
  val relationshipTypeDescription: String?,
  val relationshipToPrisoner: String?,
  val relationshipToPrisonerDescription: String?,
)
