package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonProperty

data class PrisonerContactRestrictions(
  var prisonerContactRestrictions: MutableList<PrisonerContactRestriction>? = mutableListOf(),
  var contactGlobalRestrictions: ContactGlobalRestriction? = null,
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
)
