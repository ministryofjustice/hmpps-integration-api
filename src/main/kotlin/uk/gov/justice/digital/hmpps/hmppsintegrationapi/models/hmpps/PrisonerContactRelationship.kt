package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class PrisonerContactRelationship(
  val relationshipType: String?,
  val relationshipTypeDescription: String?,
  val relationshipToPrisoner: String?,
  val relationshipToPrisonerDescription: String?,
  val approvedPrisoner: Boolean?,
  val nextOfKin: Boolean?,
  val emergencyContact: Boolean?,
  val isRelationshipActive: Boolean?,
  val currentTerm: Boolean?,
  val comments: String?,
)
