package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema

data class Contact(
  @Schema(description = "The ID of the contact", example = "123456")
  val contactId: Long,
  @Schema(description = "The last name of the contact", example = "Doe")
  val lastName: String,
  @Schema(description = "The first name of the contact", example = "John")
  val firstName: String,
  @Schema(description = "The middle names of the contact, if any", example = "William")
  val middleNames: String?,
  @Schema(description = "The date of birth of the contact, if known", example = "1980-01-01")
  val dateOfBirth: String,
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
  val postCode: String?,
  @Schema(description = "Country code", example = "ENG")
  val countryCode: String?,
  @Schema(description = "The description of country code", example = "England")
  val countryDescription: String?,
  @Schema(description = "True if this is the primary address otherwise false", example = "true")
  val primaryAddress: Boolean?,
  @Schema(description = "If true this address should be considered for sending mail to", example = "true")
  val mailAddress: Boolean?,
  @Schema(description = "Type of phone", example = "MOB")
  val phoneType: String?,
  @Schema(description = "Description of the type of phone", example = "Mobile")
  val phoneTypeDescription: String?,
  @Schema(description = "Phone number", example = "+1234567890")
  val phoneNumber: String?,
  @Schema(description = "Extension number", example = "123")
  val extNumber: String?,
)
