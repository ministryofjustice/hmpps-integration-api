package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

data class PRDetailedContact(
  val id: Long,
  val title: String?,
  val titleDescription: String?,
  val lastName: String,
  val firstName: String,
  val middleNames: String?,
  val dateOfBirth: String?,
  val isStaff: Boolean,
  val deceasedDate: String?,
  val languageCode: String?,
  val languageDescription: String?,
  val interpreterRequired: Boolean,
  val addresses: List<Address>,
  val phoneNumbers: List<PhoneNumber>,
  val emailAddresses: List<EmailAddress>,
  val gender: String,
  val genderDescription: String,
)

data class Address(
  val contactAddressId: Long,
  val contactId: Long,
  val addressType: String?,
  val addressTypeDescription: String?,
  val primaryAddress: Boolean,
  val flat: String?,
  val property: String?,
  val street: String?,
  val area: String?,
  val cityCode: String?,
  val cityDescription: String?,
  val countyCode: String?,
  val countyDescription: String?,
  val postcode: String?,
  val countryCode: String?,
  val countryDescription: String?,
  val verified: Boolean,
  val verifiedBy: String?,
  val verifiedTime: String?,
  val mailFlag: Boolean,
  val startDate: String?,
  val endDate: String?,
  val noFixedAddress: Boolean,
  val comments: String?,
  val phoneNumbers: List<PhoneNumber>,
  val createdBy: String,
  val createdTime: String,
  val updatedBy: String?,
  val updatedTime: String?,
)

data class PhoneNumber(
  val contactPhoneId: Long,
  val contactId: Long,
  val phoneType: String,
  val phoneTypeDescription: String,
  val phoneNumber: String,
  val extNumber: String?,
)

data class EmailAddress(
  val contactEmailId: Long,
  val contactId: Long,
  val emailAddress: String,
)
