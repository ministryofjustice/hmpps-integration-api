package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class DetailedContact(
  val contactId: Long,
  val titleCode: String?,
  val titleDescription: String?,
  val firstName: String,
  val lastName: String,
  val middleNames: String?,
  val dateOfBirth: String?,
  val isStaff: Boolean,
  val deceasedDate: String?,
  val languageCode: String?,
  val languageDescription: String?,
  val interpreterRequired: Boolean,
  val addresses: List<ContactAddress>,
  val phoneNumbers: List<ContactPhoneNumber>,
  val emailAddresses: List<ContactEmailAddress>,
  val genderCode: String,
  val genderDescription: String,
)

data class ContactAddress(
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
  val phoneNumbers: List<ContactPhoneNumber>,
  val createdBy: String,
  val createdTime: String,
  val updatedBy: String?,
  val updatedTime: String?,
)

data class ContactPhoneNumber(
  val phoneType: String,
  val phoneTypeDescription: String,
  val phoneNumber: String,
  val extNumber: String?,
)

data class ContactEmailAddress(
  val emailAddress: String,
)
