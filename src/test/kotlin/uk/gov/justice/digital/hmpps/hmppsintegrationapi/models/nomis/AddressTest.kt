package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateNomisTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address.Type as IntegrationAPIType

class AddressTest : DescribeSpec(
  {
    describe("#toAddress") {
      it("maps one-to-one attributes to integration API attributes") {
        val address = generateNomisTestAddress()
        val integrationApiAddress = address.toAddress()

        integrationApiAddress.country.shouldBe(address.country)
        integrationApiAddress.county.shouldBe(address.county)
        integrationApiAddress.endDate.shouldBe(address.endDate)
        integrationApiAddress.number.shouldBe(address.flat)
        integrationApiAddress.locality.shouldBe(address.locality)
        integrationApiAddress.noFixedAddress.shouldBe(address.noFixedAddress)
        integrationApiAddress.postcode.shouldBe(address.postalCode)
        integrationApiAddress.name.shouldBe(address.premise)
        integrationApiAddress.startDate.shouldBe(address.startDate)
        integrationApiAddress.street.shouldBe(address.street)
        integrationApiAddress.town.shouldBe(address.town)
        integrationApiAddress.notes.shouldBe(address.comment)
      }

      it("can deal with missing fields") {
        val address = Address(
          addressType = null,
          country = null,
          county = null,
          endDate = null,
          flat = null,
          locality = null,
          noFixedAddress = false,
          postalCode = null,
          premise = null,
          startDate = null,
          street = null,
          town = null,
          comment = null,
        )

        val integrationApiAddress = address.toAddress()

        integrationApiAddress.country.shouldBeNull()
        integrationApiAddress.county.shouldBeNull()
        integrationApiAddress.endDate.shouldBeNull()
        integrationApiAddress.number.shouldBeNull()
        integrationApiAddress.locality.shouldBeNull()
        integrationApiAddress.noFixedAddress.shouldBe(address.noFixedAddress)
        integrationApiAddress.postcode.shouldBeNull()
        integrationApiAddress.name.shouldBeNull()
        integrationApiAddress.startDate.shouldBeNull()
        integrationApiAddress.street.shouldBeNull()
        integrationApiAddress.town.shouldBeNull()
        integrationApiAddress.notes.shouldBeNull()
      }
    }
    describe("#types") {
      it("maps addressUsages only when addressType is not present") {
        val expectedCode = "abc123"
        val expectedDescription = "some description"
        val addressUsages = listOf(
          Address.AddressUsage(expectedCode, expectedDescription),
        )

        val address = generateNomisTestAddress(addressType = null, addressUsages = addressUsages)

        address.toAddress().types.shouldBe(listOf(IntegrationAPIType(expectedCode, expectedDescription)))
      }

      it("returns an empty list when neither addressType or addressUsages are present") {
        val address = generateNomisTestAddress(addressType = null, addressUsages = emptyList())

        address.toAddress().types.shouldBeEmpty()
      }

      it("addressUsage description is not present") {
        val addressUsages = listOf(Address.AddressUsage("usage1", null))

        val address = generateNomisTestAddress(addressUsages = addressUsages)

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

          val address = generateNomisTestAddress(addressType = addressType, addressUsages = addressUsages)

          address.toAddress().types.shouldContainAll(
            listOf(
              IntegrationAPIType("someAddressType", "someAddressType"),
              IntegrationAPIType("usage1", "usage description 1"),
              IntegrationAPIType("usage2", "usage description 2"),
            ),
          )
        }

        it("maps addressType only when addressUsages is not present") {
          val expectedCode = "someAddressType"
          val expectedDescription = "someAddressType"

          val address = generateNomisTestAddress(
            addressType = "someAddressType",
            addressUsages = emptyList(),
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
            val address = generateNomisTestAddress(addressType = it.key)
            address.toAddress().types.shouldContain(IntegrationAPIType(it.key, it.value))
          }
        }
      }
    }
  },
)
