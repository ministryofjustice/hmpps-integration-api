package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

data class Address(
  val postcode: String
) {
  fun toAddress(): Address = Address(postcode = this.postcode)
}
