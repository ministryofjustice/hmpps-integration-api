package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Address

data class ContactDetailsWithEmailAndPhone(
  val addresses: List<Address>?,
  val phoneNumbers: List<PhoneNumber>?,
  val emails: List<String>?,
)
