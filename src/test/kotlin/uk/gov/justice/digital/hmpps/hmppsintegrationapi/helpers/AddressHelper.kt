package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

fun generateTestAddress(
  postcode: String = "SE1 1TZ",
  country: String? = "England",
  county: String = "Greater London",
  endDate: String = "20 May 2023",
  startDate: String = "10 May 2021",
  locality: String = "London Bridge",
  name: String = "The chocolate factory",
  number: String = "89",
  street: String = "Omeara",
  town: String = "London Town",
  types: List<Address.Type> = listOf(
    Address.Type("A99", "Chocolate Factory"),
    Address.Type("B99", "Glass Elevator"),
  ),
  noFixedAddress: Boolean = false,
  notes: String = "some interesting note",
): Address = Address(
  postcode = postcode,
  country = country,
  county = county,
  endDate = endDate,
  startDate = startDate,
  locality = locality,
  name = name,
  number = number,
  street = street,
  town = town,
  types = types,
  noFixedAddress = noFixedAddress,
  notes = notes,
)
