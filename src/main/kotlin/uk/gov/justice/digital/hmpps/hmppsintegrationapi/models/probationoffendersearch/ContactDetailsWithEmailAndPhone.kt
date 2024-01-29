package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone as PersonContactDetails

class ContactDetailsWithEmailAndPhone (
  override val addresses: List<Address>,
  val phoneNumbers: List<PhoneNumber>,
  val emails: List<String>
) : ContactDetails(addresses) {

fun toContactdetails(): PersonContactDetails = PersonContactDetails(
    addresses = this.addresses,
    phoneNumbers = this.phoneNumbers,
    emails = this.emails
  )

}
