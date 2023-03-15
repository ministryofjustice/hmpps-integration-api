package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address as IntegrationAPIAddress

data class Address(
  val postcode: String?
) {
  fun toAddress() = IntegrationAPIAddress(postcode = this.postcode)
}
