package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactId

data class LinkedPrisoner(
  val prisonerNumber: String,
  val lastName: String,
  val firstName: String,
  val middleNames: String?,
  val relationships: Relationship?,
) {
  fun toPrisonerContactId() =
    PrisonerContactId(
      prisonerContactId = this.relationships?.prisonerContactId,
    )
}

data class Relationship(
  val prisonerContactId: Long?,
  val relationshipType: String?,
  val relationshipTypeDescription: String?,
  val relationshipToPrisoner: String?,
  val relationshipToPrisonerDescription: String?,
)
