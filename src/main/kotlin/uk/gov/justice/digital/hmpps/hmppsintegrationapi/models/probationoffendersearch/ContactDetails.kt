package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PhoneNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ContactDetailsWithEmailAndPhone as PersonContactDetails

class ContactDetails(
  val phoneNumbers: List<PhoneNumber>?,
  val emailAddresses: List<String>?,
) {
  fun toContactDetails(): PersonContactDetails =
    PersonContactDetails(
      phoneNumbers = this.phoneNumbers,
      emails = this.emailAddresses,
    )
}
