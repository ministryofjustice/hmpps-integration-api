package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class DetailedContact(
  @Schema(description = "The ID of the contact", example = "123456")
  val contactId: Long,
  @Schema(description = "The title code for the contact", example = "MR")
  val titleCode: String?,
  @Schema(description = "The description of the title code, if present", example = "Mr")
  val titleDescription: String?,
  @Schema(description = "The first name of the contact", example = "John")
  val firstName: String,
  @Schema(description = "The last name of the contact", example = "Doe")
  val lastName: String,
  @Schema(description = "The middle names of the contact, if any", example = "William")
  val middleNames: String?,
  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01")
  val dateOfBirth: String?,
  @Schema(description = "Whether the contact is a staff member", example = "false")
  val isStaff: Boolean,
  @Schema(description = "The date the contact deceased, if known", example = "1980-01-01")
  val deceasedDate: String?,
  @Schema(description = "The NOMIS code for the contacts language", example = "ENG")
  val languageCode: String?,
  @Schema(description = "The description of the language code", example = "English")
  val languageDescription: String?,
  @Schema(description = "Whether an interpreter is required for this contact", example = "true")
  val interpreterRequired: Boolean,
  @Schema(description = "All addresses for the contact")
  val addresses: List<ContactAddress>,
  @Schema(description = "All phone numbers for the contact")
  val phoneNumbers: List<ContactPhoneNumber>,
  @Schema(description = "All email addresses for the contact")
  val emailAddresses: List<ContactEmailAddress>,
  @Schema(description = "The NOMIS code for the contacts gender. See reference data with group code 'GENDER'")
  val genderCode: String,
  @Schema(description = "The description of gender code. See reference data with group code 'GENDER'")
  val genderDescription: String,
)

data class ContactAddress(
  @Schema(description = "The type of the address", example = "HOME")
  val addressType: String?,
  @Schema(description = "The description of the address type")
  val addressTypeDescription: String?,
  @Schema(description = "True if this is the primary address otherwise false", example = "true")
  val primaryAddress: Boolean,
  @Schema(description = "Flat number or name", example = "Flat 2B")
  val flat: String?,
  @Schema(description = "Building or house number or name", example = "Mansion House")
  val property: String?,
  @Schema(description = "Street or road name", example = "Acacia Avenue")
  val street: String?,
  @Schema(description = "Area", example = "Morton Heights")
  val area: String?,
  @Schema(description = "City code", example = "25343")
  val cityCode: String?,
  @Schema(description = "The description of the city code", example = "Sheffield")
  val cityDescription: String?,
  @Schema(description = "County code", example = "S.YORKSHIRE")
  val countyCode: String?,
  @Schema(description = "The description of county code", example = "South Yorkshire")
  val countyDescription: String?,
  @Schema(description = "Postcode", example = "S13 4FH")
  val postcode: String?,
  @Schema(description = "Country code", example = "ENG")
  val countryCode: String?,
  @Schema(description = "The description of country code", example = "England")
  val countryDescription: String?,
  @Schema(description = "Whether the address has been verified by postcode lookup", example = "false")
  val verified: Boolean,
  @Schema(description = "Which username ran the postcode lookup check", example = "NJKG44D")
  val verifiedBy: String?,
  @Schema(description = "The timestamp of when the postcode lookup was done", example = "2024-01-01T00:00:00Z")
  val verifiedTime: String?,
  @Schema(description = "Flag to indicate whether mail is allowed to be sent to this address", example = "false")
  val mailFlag: Boolean,
  @Schema(description = "The start date when this address is to be considered active from", example = "2024-01-01")
  val startDate: String?,
  @Schema(description = "The end date when this address is to be considered active from", example = "2024-01-01")
  val endDate: String?,
  @Schema(description = "Flag to indicate whether this address indicates no fixed address", example = "false")
  val noFixedAddress: Boolean,
  @Schema(description = "Any additional information or comments about the address", example = "Some additional information")
  val comments: String?,
  @Schema(description = "Phone numbers that are related to this address")
  val phoneNumbers: List<ContactPhoneNumber>,
  @Schema(description = "The id of the user who created the contact", example = "JD000001")
  val createdBy: String,
  @Schema(description = "The timestamp of when the contact was created", example = "2024-01-01T00:00:00Z")
  val createdTime: String,
  @Schema(description = "The id of the user who created the contact address", example = "JD000001")
  val updatedBy: String?,
  @Schema(description = "The timestamp of when the contact address was last updated", example = "2024-01-01T00:00:00Z")
  val updatedTime: String?,
)

data class ContactPhoneNumber(
  @Schema(description = "Type of phone", example = "MOB")
  val phoneType: String,
  @Schema(description = "Description of the type of phone", example = "Mobile")
  val phoneTypeDescription: String,
  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String,
  @Schema(description = "Extension number", example = "123")
  val extNumber: String?,
)

data class ContactEmailAddress(
  @Schema(description = "Email address", example = "test@example.com")
  val emailAddress: String,
)
