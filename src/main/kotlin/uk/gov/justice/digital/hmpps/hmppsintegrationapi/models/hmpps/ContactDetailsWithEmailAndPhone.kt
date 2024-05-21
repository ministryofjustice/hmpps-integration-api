package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class ContactDetailsWithEmailAndPhone(
  val phoneNumbers: List<PhoneNumber>?,
  val emails: List<String>?,
)
