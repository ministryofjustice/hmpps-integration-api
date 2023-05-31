package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address as IntegrationAPIAddress

data class Address(
  val addressNumber: String?,
  val district: String?,
  val buildingName: String?,
  val county: String?,
  val from: String?,
  val postcode: String?,
  val streetName: String?,
  val to: String?,
  val town: String?,
) {
  fun toAddress() = IntegrationAPIAddress(
    country = "England",
    county = this.county,
    endDate = this.to,
    locality = this.district,
    name = this.buildingName,
    number = this.addressNumber,
    postcode = this.postcode,
    startDate = this.from,
    street = this.streetName,
    town = this.town,
    type = "Type?",
  )
}
