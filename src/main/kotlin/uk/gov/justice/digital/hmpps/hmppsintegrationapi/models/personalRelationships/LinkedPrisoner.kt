package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces.IRelationship

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
  val relationships: List<LinkedPrisonerRelationship>?,
)

data class LinkedPrisonerRelationship(
  @JsonProperty("relationshipType")
  override val relationshipType: String?,
  @JsonProperty("relationshipTypeDescription")
  override val relationshipTypeDescription: String?,
  @JsonProperty("relationshipToPrisoner")
  override val relationshipToPrisoner: String?,
  @JsonProperty("relationshipToPrisonerDescription")
  override val relationshipToPrisonerDescription: String?,
  @JsonProperty("prisonerContactId")
  val prisonerContactId: Long?,
) : IRelationship
