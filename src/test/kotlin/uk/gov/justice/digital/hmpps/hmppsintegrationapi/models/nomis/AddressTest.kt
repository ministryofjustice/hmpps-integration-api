package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AddressTest : DescribeSpec(
  {
    describe("#toAddress") {
      it("maps one-to-one attributes to integration API attributes") {
        val address = Address(
          addressType = "addressType",
          country = "country",
          county = "county",
          endDate = "endDate",
          flat = "flat",
          locality = "locality",
          postalCode = "postalCode",
          premise = "premise",
          startDate = "startDate",
          street = "street",
          town = "town",
        )

        val integrationApiAddress = address.toAddress()

        integrationApiAddress.country.shouldBe(address.country)
        integrationApiAddress.county.shouldBe(address.county)
        integrationApiAddress.endDate.shouldBe(address.endDate)
        integrationApiAddress.number.shouldBe(address.flat)
        integrationApiAddress.locality.shouldBe(address.locality)
        integrationApiAddress.postcode.shouldBe(address.postalCode)
        integrationApiAddress.name.shouldBe(address.premise)
        integrationApiAddress.startDate.shouldBe(address.startDate)
        integrationApiAddress.street.shouldBe(address.street)
        integrationApiAddress.town.shouldBe(address.town)
      }
    }
    describe("#types") {
      var address = Address(
        addressType = "addressType",
        country = "country",
        county = "county",
        endDate = "endDate",
        flat = "flat",
        locality = "locality",
        postalCode = "postalCode",
        premise = "premise",
        startDate = "startDate",
        street = "street",
        town = "town",
        addressUsages = emptyList(),
      )
      it("maps addressUsages only when addressType is not present") {
        address.addressType = null
      }
//      it("maps addressType only when addressUsages is not present") {
//
//      }
//      it("maps addressUsages and addressType combined when both are present") {
//
//      }
//      it("returns an empty list when neither addressType or addressUsages are present") {
//
//      }
    }
  },
)
