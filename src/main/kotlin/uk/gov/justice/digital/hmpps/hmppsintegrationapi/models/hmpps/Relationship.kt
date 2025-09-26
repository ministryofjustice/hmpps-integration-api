package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships.interfaces.IRelationship

data class Relationship(
  @JsonProperty("relationshipTypeCode")
  override val relationshipTypeCode: String,
  @JsonProperty("relationshipTypeDescription")
  override val relationshipTypeDescription: String,
  @JsonProperty("relationshipToPrisonerCode")
  override val relationshipToPrisonerCode: String,
  @JsonProperty("relationshipToPrisonerDescription")
  override val relationshipToPrisonerDescription: String?,
  @JsonProperty("approvedVisitor")
  val approvedPrisoner: Boolean?,
  @JsonProperty("nextOfKin")
  val nextOfKin: Boolean?,
  @JsonProperty("emergencyContact")
  val emergencyContact: Boolean?,
  @JsonProperty("isRelationshipActive")
  override val isRelationshipActive: Boolean,
  @JsonProperty("currentTerm")
  val currentTerm: Boolean?,
  @JsonProperty("comments")
  val comments: String?,
) : IRelationship
