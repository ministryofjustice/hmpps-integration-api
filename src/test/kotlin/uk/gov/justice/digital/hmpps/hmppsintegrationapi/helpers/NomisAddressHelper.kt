package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Address

fun generateNomisTestAddress(
  addressType: String? = "test_addresstype",
  country: String? = "test_country",
  county: String? = "test_county",
  endDate: String? = "test_enddate",
  flat: String? = "test_flat",
  locality: String? = "test_locality",
  noFixedAddress: Boolean = false,
  postalCode: String? = "test_postcode",
  premise: String? = "test_premise",
  startDate: String? = "test_startdate",
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
  endDate = endDate,
  flat = flat,
  locality = locality,
  noFixedAddress = noFixedAddress,
  postalCode = postalCode,
  premise = premise,
  startDate = startDate,
  street = street,
  town = town,
  comment = comment,
  addressUsages = addressUsages,
)
