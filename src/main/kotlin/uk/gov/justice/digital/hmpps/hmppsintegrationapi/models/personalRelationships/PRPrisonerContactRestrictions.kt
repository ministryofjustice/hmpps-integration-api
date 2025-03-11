package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactGlobalRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRestriction

data class PRPrisonerContactRestrictions(
  var prisonerContactRestrictions: List<PRPrisonerContactRestriction>? = emptyList(),
  var contactGlobalRestrictions: List<PRContactGlobalRestriction>? = emptyList(),
)

data class PRPrisonerContactRestriction(
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
) {
  fun toPrisonerContactRestriction() =
    PrisonerContactRestriction(
      prisonerContactRestrictionId = this.prisonerContactRestrictionId,
      prisonerContactId = this.prisonerContactId,
      contactId = this.contactId,
      prisonerNumber = this.prisonerNumber,
      restrictionType = this.restrictionType,
      restrictionTypeDescription = this.restrictionTypeDescription,
      startDate = this.startDate,
      expiryDate = this.expiryDate,
      comments = this.comments,
      enteredByUsername = this.enteredByUsername,
      enteredByDisplayName = this.enteredByDisplayName,
      createdBy = this.createdBy,
      createdTime = this.createdTime,
      updatedBy = this.updatedBy,
      updatedTime = this.updatedTime,
    )
}

data class PRContactGlobalRestriction(
  @JsonProperty("contactRestrictionId")
  val contactRestrictionId: Long,
  @JsonProperty("contactId")
  val contactId: Long,
  @JsonProperty("restrictionType")
  val restrictionType: String,
  @JsonProperty("restrictionTypeDescription")
  val restrictionTypeDescription: String,
  @JsonProperty("startDate")
  val startDate: String,
  @JsonProperty("expiryDate")
  val expiryDate: String,
  @JsonProperty("comments")
  val comments: String,
  @JsonProperty("enteredByUsername")
  val enteredByUsername: String,
  @JsonProperty("enteredByDisplayName")
  val enteredByDisplayName: String,
  @JsonProperty("createdBy")
  val createdBy: String,
  @JsonProperty("createdTime")
  val createdTime: String,
  @JsonProperty("updatedBy")
  val updatedBy: String,
  @JsonProperty("updatedTime")
  val updatedTime: String,
) {
  fun toContactGlobalRestriction() =
    ContactGlobalRestriction(
      contactRestrictionId = this.contactRestrictionId,
      contactId = this.contactId,
      restrictionType = this.restrictionType,
      restrictionTypeDescription = this.restrictionTypeDescription,
      startDate = this.startDate,
      expiryDate = this.expiryDate,
      comments = this.comments,
      enteredByUsername = this.enteredByUsername,
      enteredByDisplayName = this.enteredByDisplayName,
      createdBy = this.createdBy,
      createdTime = this.createdTime,
      updatedBy = this.updatedBy,
      updatedTime = this.updatedTime,
    )
}
