package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.personalRelationships

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactEmailAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactPhoneNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DetailedContact

data class PRDetailedContact(
  val id: Long,
  val titleCode: String?,
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
  val genderCode: String,
  val genderDescription: String,
) {
  fun toDetailedContact(): DetailedContact =
    DetailedContact(
      contactId = this.id,
      titleCode = this.titleCode,
      titleDescription = this.titleDescription,
      firstName = this.firstName,
      lastName = this.lastName,
      middleNames = this.middleNames,
      dateOfBirth = this.dateOfBirth,
      isStaff = this.isStaff,
      deceasedDate = this.deceasedDate,
      languageCode = this.languageCode,
      languageDescription = this.languageDescription,
      interpreterRequired = this.interpreterRequired,
      addresses = this.addresses.map { it.toContactAddress() },
      phoneNumbers = this.phoneNumbers.map { it.toContactPhoneNumber() },
      emailAddresses = this.emailAddresses.map { it.toContactEmailAddress() },
      genderCode = this.genderCode,
      genderDescription = this.genderDescription,
    )
}

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
) {
  fun toContactAddress(): ContactAddress =
    ContactAddress(
      addressType = this.addressType,
      addressTypeDescription = this.addressTypeDescription,
      primaryAddress = this.primaryAddress,
      flat = this.flat,
      property = this.property,
      street = this.street,
      area = this.area,
      cityCode = this.cityCode,
      cityDescription = this.cityDescription,
      countryCode = this.countryCode,
      countryDescription = this.countryDescription,
      countyCode = this.countyCode,
      countyDescription = this.countyDescription,
      postcode = this.postcode,
      verified = this.verified,
      verifiedBy = this.verifiedBy,
      verifiedTime = this.createdTime,
      mailFlag = this.mailFlag,
      startDate = this.startDate,
      endDate = this.endDate,
      noFixedAddress = this.noFixedAddress,
      comments = this.comments,
      phoneNumbers = this.phoneNumbers.map { it.toContactPhoneNumber() },
      createdBy = this.createdBy,
      createdTime = this.createdTime,
      updatedBy = this.updatedBy,
      updatedTime = this.updatedTime,
    )
}

data class PhoneNumber(
  val contactPhoneId: Long,
  val contactId: Long,
  val phoneType: String,
  val phoneTypeDescription: String,
  val phoneNumber: String,
  val extNumber: String?,
) {
  fun toContactPhoneNumber(): ContactPhoneNumber =
    ContactPhoneNumber(
      phoneType = this.phoneType,
      phoneTypeDescription = this.phoneTypeDescription,
      phoneNumber = this.phoneNumber,
      extNumber = this.extNumber,
    )
}

data class EmailAddress(
  val contactEmailId: Long,
  val contactId: Long,
  val emailAddress: String,
) {
  fun toContactEmailAddress(): ContactEmailAddress = ContactEmailAddress(emailAddress = this.emailAddress)
}
