package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address as IntegrationAPIAddress

data class Address(
  val postalCode: String,
) {
  fun toAddress() = IntegrationAPIAddress(postcode = this.postalCode)
}
