package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address.Type as IntegrationAPIAddressType

class AddressTest : DescribeSpec(
  {
    describe("#toAddress") {
      it("maps one-to-one attributes to integration API attributes") {
        val someCode = "some description"
        val someDescription = "some Description"

        val address = Address(
          addressNumber = "addressNumber",
          buildingName = "buildingName",
          district = "district",
          county = "county",
          from = LocalDate.parse("2020-11-11"),
          postcode = "postcode",
          streetName = "streetName",
          type = Address.Type(code = someCode, description = someDescription),
          to = LocalDate.parse("2021-01-01"),
          town = "town",
          noFixedAbode = true,
          notes = "notes",
        )

        val integrationApiAddress = address.toAddress()

        integrationApiAddress.country.shouldBeNull()
        integrationApiAddress.county.shouldBe(address.county)
        integrationApiAddress.endDate.shouldBe(address.to)
        integrationApiAddress.number.shouldBe(address.addressNumber)
        integrationApiAddress.locality.shouldBe(address.district)
        integrationApiAddress.postcode.shouldBe(address.postcode)
        integrationApiAddress.name.shouldBe(address.buildingName)
        integrationApiAddress.startDate.shouldBe(address.from)
        integrationApiAddress.types.shouldBe(listOf(IntegrationAPIAddressType(someCode, someDescription)))
        integrationApiAddress.street.shouldBe(address.streetName)
        integrationApiAddress.town.shouldBe(address.town)
        integrationApiAddress.noFixedAddress.shouldBe(address.noFixedAbode)
        integrationApiAddress.notes.shouldBe(address.notes)
      }

      it("uses the code as the description if the description is not present") {
        val someCode = "some description"

        val address = Address(
          addressNumber = "addressType",
          buildingName = "buildingName",
          district = "country",
          county = "county",
          from = LocalDate.parse("2020-01-01"),
          postcode = "flat",
          streetName = "locality",
          type = Address.Type(someCode, null),
          to = LocalDate.parse("2021-01-01"),
          town = "town",
          noFixedAbode = true,
          notes = "notes",
        )

        address.toAddress().types.shouldBe(listOf(IntegrationAPIAddressType(someCode, someCode)))
      }
    }
  },
)
