package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Address
import java.time.LocalDate

fun generateNomisTestAddress(
  addressType: String? = "test_addresstype",
  country: String? = "test_country",
  county: String? = "test_county",
  endDate: String? = "2022-05-01",
  flat: String? = "test_flat",
  locality: String? = "test_locality",
  noFixedAddress: Boolean = false,
  postalCode: String? = "test_postcode",
  premise: String? = "test_premise",
  startDate: String? = "2021-05-01",
  street: String? = "test_street",
  town: String? = "test_town",
  comment: String? = "test_comment",
  addressUsages: List<Address.AddressUsage> = listOf(
    Address.AddressUsage(
      addressUsage = "test_addressusage",
      addressUsageDescription = "test_addressusagedescription",
    ),
    Address.AddressUsage(
      addressUsage = "test_addressusage_02",
      addressUsageDescription = "test_addressusagedescription_02",
    ),
  ),
): Address = Address(
  addressType = addressType,
  country = country,
  county = county,
  endDate = LocalDate.parse(endDate),
  flat = flat,
  locality = locality,
  noFixedAddress = noFixedAddress,
  postalCode = postalCode,
  premise = premise,
  startDate = LocalDate.parse(startDate),
  street = street,
  town = town,
  comment = comment,
  addressUsages = addressUsages,
)
