package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty

data class PRLinkedPrisoner(
  @JsonProperty("prisonerNumber")
  val prisonerNumber: String,
  @JsonProperty("lastName")
  val lastName: String,
  @JsonProperty("firstName")
  val firstName: String,
  @JsonProperty("middleNames")
  val middleNames: String?,
  @JsonProperty("relationshipTypeCode")
  val relationshipTypeCode: String,
  @JsonProperty("relationshipTypeDescription")
  val relationshipTypeDescription: String,
  @JsonProperty("relationshipToPrisonerCode")
  val relationshipToPrisonerCode: String,
  @JsonProperty("relationshipToPrisonerDescription")
  val relationshipToPrisonerDescription: String?,
  @JsonProperty("isRelationshipActive")
  val isRelationshipActive: Boolean,
  @JsonProperty("prisonerContactId")
  val prisonerContactId: Long,
)
