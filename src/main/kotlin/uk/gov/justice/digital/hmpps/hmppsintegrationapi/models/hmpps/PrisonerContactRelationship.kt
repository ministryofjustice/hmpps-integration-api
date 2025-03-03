package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PrisonerContactRelationship(
  val relationshipTypeCode: String?,
  val relationshipTypeDescription: String?,
  val relationshipToPrisonerCode: String?,
  val relationshipToPrisonerDescription: String?,
  val approvedVisitor: Boolean?,
  val nextOfKin: Boolean?,
  val emergencyContact: Boolean?,
  val isRelationshipActive: Boolean?,
  val currentTerm: Boolean?,
  val comments: String?,
)
