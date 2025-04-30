package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.REPLACE_PROBATION_SEARCH
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetAddressesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val nDeliusMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)
      val hmppsId = "X777776"
      val path = "/case/$hmppsId/addresses"

      beforeEach {
        whenever(featureFlag.isEnabled(REPLACE_PROBATION_SEARCH)).thenReturn(true)

        nDeliusMockServer.start()
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "contactDetails": {
              "addresses": [
                {
                  "id": 123456,
                  "county": "Greater London",
                  "from": "2021-10-10",
                  "to": "2022-01-01",
                  "noFixedAbode": false,
                  "notes": "some interesting note",
                  "addressNumber": "89",
                  "streetName": "Omeara",
                  "district": "London Bridge",
                  "town": "London Town",
                  "buildingName": "The chocolate factory",
                  "postcode": "SE1 1TZ",
                  "type": {
                    "code": "A07",
                    "description": "Friends/Family"
                  },
                  "status": {
                    "code": "P",
                    "description": "Previous"
                  }
                }
              ]
            }
          }
      """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        nDeliusMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        nDeliusGateway.getAddressesForPerson(hmppsId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns addresses for a person with the matching ID") {
        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data.shouldContain(
          generateTestAddress(
            country = null,
            endDate = "2022-01-01",
            startDate = "2021-10-10",
            types = listOf(Address.Type("A07", "Friends/Family")),
          ),
        )
      }

      it("returns an empty list when no addresses are found") {
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "contactDetails": {
              "addresses": []
            }
          }
        """,
        )

        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data.shouldBeEmpty()
      }

      it("returns an empty list when there is no contactDetails field") {
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "firstName": "English",
            "surname": "Breakfast"
          }
        """,
        )

        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data.shouldBeEmpty()
      }

      it("returns an empty list when contactDetails field is null") {
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "contactDetails": null
          }
        """,
        )

        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data.shouldBeEmpty()
      }

      it("returns an empty list when contactDetails.addresses field is null") {
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "contactDetails": {
              "addresses": null
            }
          }
        """,
        )

        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data.shouldBeEmpty()
      }

      it("returns an empty list when the type is an empty object") {
        nDeliusMockServer.stubForGet(
          path,
          """
          {
            "contactDetails": {
               "addresses": [
                {
                    "type": {}
                }
              ]
            }
          }
        """,
        )

        val response = nDeliusGateway.getAddressesForPerson(hmppsId)

        response.data
          .first()
          .types
          .shouldBeEmpty()
      }
    },
  )
