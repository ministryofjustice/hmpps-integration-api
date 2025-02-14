package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class PrisonerContactRestrictions(
  val prisonerContactRestrictions: List<PrisonerContactRestriction>?,
  val contactGlobalRestrictions: List<ContactGlobalRestriction>?,
)

data class PrisonerContactRestriction(
  val prisonerContactRestrictionId: Long,
  val prisonerContactId: Long,
  val contactId: Long,
  val prisonerNumber: String,
  val restrictionType: String,
  val restrictionTypeDescription: String,
  val startDate: String,
  val expiryDate: String,
  val comments: String,
  val enteredByUsername: String,
  val enteredByDisplayName: String,
  val createdBy: String,
  val createdTime: String,
  val updatedBy: String,
  val updatedTime: String,
)

data class ContactGlobalRestriction(
  val contactRestrictionId: Long,
  val contactId: Long,
  val restrictionType: String,
  val restrictionTypeDescription: String,
  val startDate: String,
  val expiryDate: String,
  val comments: String,
  val enteredByUsername: String,
  val enteredByDisplayName: String,
  val createdBy: String,
  val createdTime: String,
  val updatedBy: String,
  val updatedTime: String,
)
