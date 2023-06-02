package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address as IntegrationAPIAddress

data class Address(
  val addressType: String?,
  val country: String?,
  val county: String?,
  val endDate: String?,
  val flat: String?,
  val locality: String?,
  val postalCode: String?,
  val premise: String?,
  val startDate: String?,
  val street: String?,
  val town: String?,
) {
  fun toAddress() = IntegrationAPIAddress(
    country = this.country,
    county = this.county,
    endDate = this.endDate,
    locality = this.locality,
    name = this.premise,
    number = this.flat,
    postcode = this.postalCode,
    startDate = this.startDate,
    street = this.street,
    town = this.town,
    type = this.addressType,
  )
}
