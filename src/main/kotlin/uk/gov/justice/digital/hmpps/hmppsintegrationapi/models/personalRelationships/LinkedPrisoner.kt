package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactId

data class LinkedPrisoner(
  @JsonProperty("prisonerNumber")
  val prisonerNumber: String,
  @JsonProperty("lastName")
  val lastName: String,
  @JsonProperty("firstName")
  val firstName: String,
  @JsonProperty("middleNames")
  val middleNames: String?,
  @JsonProperty("relationships")
  val relationships: List<Relationship>?,
) {
  fun toPrisonerContactId(): List<PrisonerContactId> = relationships?.map { relationship -> PrisonerContactId(relationship.prisonerContactId) } ?: emptyList()
}

data class Relationship(
  @JsonProperty("prisonerContactId")
  val prisonerContactId: Long?,
  @JsonProperty("relationshipType")
  val relationshipType: String?,
  @JsonProperty("relationshipTypeDescription")
  val relationshipTypeDescription: String?,
  @JsonProperty("relationshipToPrisoner")
  val relationshipToPrisoner: String?,
  @JsonProperty("relationshipToPrisonerDescription")
  val relationshipToPrisonerDescription: String?,
)
