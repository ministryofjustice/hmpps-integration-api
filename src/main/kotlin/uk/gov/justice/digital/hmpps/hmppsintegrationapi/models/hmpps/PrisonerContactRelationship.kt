package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class PrisonerContactRelationship(
  @Schema(description = "Coded value indicating either a social or official contact. This is a coded value from the group code CONTACT_TYPE in reference data", example = "Social")
  val relationshipTypeCode: String?,
  @Schema(description = "The description of the relationship type", example = "Friend")
  val relationshipTypeDescription: String?,
  @Schema(description = "The relationship to the prisoner. A code from SOCIAL_RELATIONSHIP or OFFICIAL_RELATIONSHIP reference data groups depending on the relationship type", example = "FRI")
  val relationshipToPrisonerCode: String?,
  @Schema(description = "The description of the relationship to the prisoner", example = "Friend")
  val relationshipToPrisonerDescription: String?,
  @Schema(description = "Indicates whether the contact is an approved visitor", example = "true")
  val approvedVisitor: Boolean?,
  @Schema(description = "Is this contact the prisoner's next of kin?", example = "false")
  val nextOfKin: Boolean?,
  @Schema(description = "Is this contact the prisoner's emergency contact?", example = "true")
  val emergencyContact: Boolean?,
  @Schema(description = "Is this contact the prisoner's next of kin?", example = "true")
  val isRelationshipActive: Boolean?,
  @Schema(description = "Is this relationship active for the current booking?", example = "true")
  val currentTerm: Boolean?,
  @Schema(description = "Any additional comments", example = "Close family friend")
  val comments: String?,
)
