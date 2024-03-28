package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import java.time.LocalDate

data class Address(
  val addressNumber: String?,
  val district: String?,
  val buildingName: String?,
  val county: String?,
  val from: LocalDate?,
  val postcode: String?,
  val streetName: String?,
  val type: Type,
  val to: LocalDate?,
  val town: String?,
  val noFixedAbode: Boolean,
  val notes: String?,
) {
  fun toAddress() =
    Address(
      country = null,
      county = this.county,
      endDate = this.to,
      locality = this.district,
      name = this.buildingName,
      noFixedAddress = this.noFixedAbode,
      number = this.addressNumber,
      postcode = this.postcode,
      startDate = this.from,
      street = this.streetName,
      town = this.town,
      types = if (this.type.code == null) emptyList() else listOf(Type(type.code, type.description).toAddressType()),
      notes = this.notes,
    )

  data class Type(
    val code: String?,
    val description: String?,
  ) {
    fun toAddressType() =
      Address.Type(
        code = this.code,
        description = this.description ?: this.code,
      )
  }
}
