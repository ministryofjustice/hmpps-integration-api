package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

data class Address(
  val postalCode: String
) {
  fun toAddress() = Address(postcode = this.postalCode)
}
