package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Contact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerContactRelationship

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
  @JsonProperty("relationshipTypeCode")
  val relationshipTypeCode: String,
  @JsonProperty("relationshipTypeDescription")
  val relationshipTypeDescription: String,
  @JsonProperty("relationshipToPrisonerCode")
  val relationshipToPrisonerCode: String,
  @JsonProperty("relationshipToPrisonerDescription")
  val relationshipToPrisonerDescription: String,
  @JsonProperty("flat")
  val flat: String?,
  @JsonProperty("property")
  val property: String?,
  @JsonProperty("street")
  val street: String?,
  @JsonProperty("area")
  val area: String?,
  @JsonProperty("cityCode")
  val cityCode: String?,
  @JsonProperty("cityDescription")
  val cityDescription: String?,
  @JsonProperty("countyCode")
  val countyCode: String?,
  @JsonProperty("countyDescription")
  val countyDescription: String?,
  @JsonProperty("postcode")
  val postCode: String?,
  @JsonProperty("countryCode")
  val countryCode: String?,
  @JsonProperty("countryDescription")
  val countryDescription: String?,
  @JsonProperty("primaryAddress")
  val primaryAddress: Boolean?,
  @JsonProperty("mailAddress")
  val mailAddress: Boolean?,
  @JsonProperty("phoneType")
  val phoneType: String?,
  @JsonProperty("phoneTypeDescription")
  val phoneTypeDescription: String?,
  @JsonProperty("phoneNumber")
  val phoneNumber: String?,
  @JsonProperty("extNumber")
  val extNumber: String?,
  @JsonProperty("isApprovedVisitor")
  val isApprovedVisitor: Boolean,
  @JsonProperty("isNextOfKin")
  val isNextOfKin: Boolean,
  @JsonProperty("isEmergencyContact")
  val isEmergencyContact: Boolean,
  @JsonProperty("isRelationshipActive")
  val isRelationshipActive: Boolean,
  @JsonProperty("currentTerm")
  val currentTerm: Boolean,
  @JsonProperty("comments")
  val comments: String?,
  @Schema(description = "Any restriction summary details")
  val restrictionSummary: RestrictionSummary?,
) {
  fun toPrisonerContact(): PrisonerContact =
    PrisonerContact(
      contact =
        Contact(
          contactId = this.contactId,
          firstName = this.firstName,
          lastName = this.lastName,
          middleNames = this.middleNames,
          dateOfBirth = this.dateOfBirth,
          flat = this.flat,
          property = this.property,
          street = this.street,
          area = this.area,
          cityCode = this.cityCode,
          cityDescription = this.cityDescription,
          countyCode = this.countyCode,
          countyDescription = this.countyDescription,
          postCode = this.postCode,
          countryCode = this.countryCode,
          countryDescription = this.countryDescription,
          primaryAddress = this.primaryAddress,
          mailAddress = this.mailAddress,
          phoneType = this.phoneType,
          phoneTypeDescription = this.phoneTypeDescription,
          phoneNumber = this.phoneNumber,
          extNumber = this.extNumber,
        ),
      relationship =
        PrisonerContactRelationship(
          relationshipTypeCode = this.relationshipTypeCode,
          relationshipTypeDescription = this.relationshipTypeDescription,
          relationshipToPrisonerCode = this.relationshipToPrisonerCode,
          relationshipToPrisonerDescription = this.relationshipToPrisonerDescription,
          approvedVisitor = this.isApprovedVisitor,
          nextOfKin = this.isNextOfKin,
          emergencyContact = this.isEmergencyContact,
          isRelationshipActive = this.isRelationshipActive,
          currentTerm = this.currentTerm,
          comments = this.comments,
        ),
    )
}

data class RestrictionSummary(
  val active: List<RestrictionTypeDetails?>,
  val totalActive: Int?,
  val totalExpired: Int?,
)

data class RestrictionTypeDetails(
  val restrictionType: String?,
  val restrictionTypeDescription: String?,
)
