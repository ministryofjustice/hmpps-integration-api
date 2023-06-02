package uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

fun generateTestAddress(
  postcode: String = "SE1 1TZ",
  country: String = "England",
  county: String = "Greater London",
  endDate: String = "20 May 2023",
  startDate: String = "10 May 2021",
  locality: String = "London Bridge",
  name: String = "The chocolate factory",
  number: String = "89",
  street: String = "Omeara",
  town: String = "London Town",
  type: String = "Type?",
): Address {
  return Address(
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
    type = type,
  )
}
