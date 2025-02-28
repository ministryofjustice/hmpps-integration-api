package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty

data class PRPrisonerContact(
  @JsonProperty("prisonerContactId")
  val prisonerContactId: Long,
  @JsonProperty("contactId")
  val contactId: Long,
  @JsonProperty("prisonerNumber")
  val prisonerNumber: String,
  @JsonProperty("lastName")
  val lastName: String,
  @JsonProperty("firstName")
  val firstName: String,
  @JsonProperty("middleNames")
  val middleNames: String?,
  @JsonProperty("dateOfBirth")
  val dateOfBirth: String,
  @JsonProperty("relationshipType")
  val relationshipType: String,
  @JsonProperty("relationshipTypeDescription")
  val relationshipTypeDescription: String,
  @JsonProperty("relationshipToPrisoner")
  val relationshipToPrisoner: String,
  @JsonProperty("relationshipToPrisonerDescription")
  val relationshipToPrisonerDescription: String,
  @JsonProperty("flat")
  val flat: String?,
  @JsonProperty("property")
  val property: String?,
  @JsonProperty("street")
  val street: String,
  @JsonProperty("area")
  val area: String?,
  @JsonProperty("cityCode")
  val cityCode: String?,
  @JsonProperty("cityDescription")
  val cityDescription: String,
  @JsonProperty("countyCode")
  val countyCode: String?,
  @JsonProperty("countyDescription")
  val countyDescription: String,
  @JsonProperty("postCode")
  val postCode: String,
  @JsonProperty("countryCode")
  val countryCode: String,
  @JsonProperty("countryDescription")
  val countryDescription: String,
  @JsonProperty("primaryAddress")
  val primaryAddress: Boolean,
  @JsonProperty("mailAddress")
  val mailAddress: Boolean,
  @JsonProperty("phoneType")
  val phoneType: String,
  @JsonProperty("phoneTypeDescription")
  val phoneTypeDescription: String,
  @JsonProperty("phoneNumber")
  val phoneNumber: String,
  @JsonProperty("extNumber")
  val extNumber: String?,
  @JsonProperty("approvedVisitor")
  val approvedVisitor: Boolean,
  @JsonProperty("nextOfKin")
  val nextOfKin: Boolean,
  @JsonProperty("emergencyContact")
  val emergencyContact: Boolean,
  @JsonProperty("isRelationshipActive")
  val isRelationshipActive: Boolean,
  @JsonProperty("currentTerm")
  val currentTerm: Boolean,
  @JsonProperty("comments")
  val comments: String?,
)
