package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ContactDetails (

  val phoneNumbers: List<PhoneNumber> = emptyList(),
  val emailAddresses:  List<String> = emptyList()

)
