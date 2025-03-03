package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces.IRelationship

data class Relationship(
  @JsonProperty("relationshipType")
  override val relationshipType: String?,
  @JsonProperty("relationshipTypeDescription")
  override val relationshipTypeDescription: String?,
  @JsonProperty("relationshipToPrisoner")
  override val relationshipToPrisoner: String?,
  @JsonProperty("relationshipToPrisonerDescription")
  override val relationshipToPrisonerDescription: String?,
  @JsonProperty("approvedVisitor")
  val approvedPrisoner: Boolean?,
  @JsonProperty("nextOfKin")
  val nextOfKin: Boolean?,
  @JsonProperty("emergencyContact")
  val emergencyContact: Boolean?,
  @JsonProperty("isRelationshipActive")
  val isRelationshipActive: Boolean?,
  @JsonProperty("currentTerm")
  val currentTerm: Boolean?,
  @JsonProperty("comments")
  val comments: String?,
) : IRelationship
