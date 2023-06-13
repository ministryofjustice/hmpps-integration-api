package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address.Type as IntegrationAPIType

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
      it("maps addressUsages only when addressType is not present") {
        val addressType = null
        val expectedCode = "abc123"
        val expectedDescription = "some description"
        val addressUsages = listOf(
          Address.AddressUsage(expectedCode, expectedDescription),
        )

        val address = Address(
          addressType = addressType,
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
          addressUsages = addressUsages,
        )

        address.toAddress().types.shouldBe(listOf(IntegrationAPIType(expectedCode, expectedDescription)))
      }

      it("returns an empty list when neither addressType or addressUsages are present") {
        val addressType = null
        val addressUsages = emptyList<Address.AddressUsage>()

        val address = Address(
          addressType = addressType,
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
          addressUsages = addressUsages,
        )

        address.toAddress().types.shouldBeEmpty()
      }

      it("addressUsage description is not present") {
        val addressUsage = Address.AddressUsage("usage1", null)

        val address = Address(
          addressType = null,
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
          addressUsages = listOf(addressUsage),
        )

        address.toAddress().types.shouldContain(
          IntegrationAPIType(
            "usage1",
            "usage1",
          ),
        )
      }

      describe("when addressType is present") {
        it("maps addressUsages and addressType combined when both are present") {
          val addressType = "someAddressType"
          val addressUsages = listOf(
            Address.AddressUsage("usage1", "usage description 1"),
            Address.AddressUsage("usage2", "usage description 2"),
          )

          val address = Address(
            addressType = addressType,
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
            addressUsages = addressUsages,
          )

          address.toAddress().types.shouldContainAll(
            listOf(
              IntegrationAPIType("someAddressType", "someAddressType"),
              IntegrationAPIType("usage1", "usage description 1"),
              IntegrationAPIType("usage2", "usage description 2"),
            ),
          )
        }

        it("maps addressType only when addressUsages is not present") {
          val addressType = "someAddressType"
          val addressUsages = emptyList<Address.AddressUsage>()
          val expectedCode = "someAddressType"
          val expectedDescription = "someAddressType"

          val address = Address(
            addressType = addressType,
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
            addressUsages = addressUsages,
          )

          address.toAddress().types.shouldBe(listOf(IntegrationAPIType(expectedCode, expectedDescription)))
        }

        it("maps descriptions") {
          val codeAndDescription = mapOf(
            "BUS" to "Business Address",
            "HOME" to "Home Address",
            "WORK" to "Work Address",
          )

          codeAndDescription.forEach { it ->
            val address = Address(
              addressType = it.key,
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

            address.toAddress().types.shouldContain(IntegrationAPIType(it.key, it.value))
          }
        }
      }
    }
  },
)
