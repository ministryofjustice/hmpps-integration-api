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
  val addressUsages: List<AddressUsage> = emptyList(),
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
    types = addressTypes(this.addressUsages, this.addressType)
  )

  private fun addressTypes(addressUsages: List<AddressUsage>, addressType: String?) : List<IntegrationAPIAddress.Type> {
    val result = addressUsages.map { it.toAddressType() }.plus(
      IntegrationAPIAddress.Type(
        code = addressType,
        description = getAddressDescriptionFromType(addressType),
      ),
    )

    return result.filter { it.code != null }
  }

  private fun getAddressDescriptionFromType(addressType: String?): String? {
    return when (addressType) {
      "BUS" -> "Business Address"
      "HOME" -> "Home Address"
      "WORK" -> "Work Address"
      else -> addressType
    }
  }

  data class AddressUsage(
    val addressUsage: String,
    val addressUsageDescription: String,
  ) {
    fun toAddressType() = IntegrationAPIAddress.Type(
      code = this.addressUsage,
      description = this.addressUsageDescription,
    )
  }
}
