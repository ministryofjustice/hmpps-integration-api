package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import java.time.LocalDate

data class NomisAddress(
  val addressType: String?,
  val country: String?,
  val county: String?,
  val endDate: LocalDate?,
  val flat: String?,
  val locality: String?,
  val noFixedAddress: Boolean,
  val postalCode: String?,
  val premise: String?,
  val startDate: LocalDate?,
  val street: String?,
  val town: String?,
  val addressUsages: List<AddressUsage> = emptyList(),
  val comment: String?,
) {
  fun toAddress() =
    Address(
      country = this.country,
      county = this.county,
      endDate = this.endDate,
      locality = this.locality,
      name = this.premise,
      noFixedAddress = this.noFixedAddress,
      number = this.flat,
      postcode = this.postalCode,
      startDate = this.startDate,
      street = this.street,
      town = this.town,
      types = addressTypes(this.addressUsages, this.addressType),
      notes = this.comment,
    )

  private fun addressTypes(
    addressUsages: List<AddressUsage>,
    addressType: String?,
  ): List<Address.Type> {
    val result =
      addressUsages.map { it.toAddressType() }.plus(
        Address.Type(
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
    val addressUsageDescription: String?,
  ) {
    fun toAddressType() =
      Address.Type(
        code = this.addressUsage,
        description = this.addressUsageDescription ?: this.addressUsage,
      )
  }
}
