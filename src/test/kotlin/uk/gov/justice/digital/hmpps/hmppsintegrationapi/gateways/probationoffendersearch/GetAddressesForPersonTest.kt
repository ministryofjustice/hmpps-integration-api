package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestAddress
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address as HmppsAddress

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class],
)
class GetAddressesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec(
  {
    val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()
    val hmppsId = "2002/1121M"

    beforeEach {
      probationOffenderSearchApiMockServer.start()
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
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
        ]
      """,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("Probation Offender Search")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }

    afterTest {
      probationOffenderSearchApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
    }

    it("returns addresses for a person with the matching ID") {
      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldContain(
        generateTestAddress(
          country = null,
          endDate = "2022-01-01",
          startDate = "2021-10-10",
          types = listOf(HmppsAddress.Type("A07", "Friends/Family")),
        ),
      )
    }

    it("returns an empty list when no addresses are found") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "contactDetails": {
              "addresses": []
            }
          }
        ]
        """,
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldBeEmpty()
    }

    it("returns an error when no results are returned") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        "[]",
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldBeEmpty()
    }

    it("returns an empty list when there is no contactDetails field") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast"
          }
        ]
        """,
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldBeEmpty()
    }

    it("returns an empty list when contactDetails field is null") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "contactDetails": null
          }
        ]
        """,
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldBeEmpty()
    }

    it("returns an empty list when contactDetails.addresses field is null") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "contactDetails": {
              "addresses": null
            }
          }
        ]
        """,
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.shouldBeEmpty()
    }

    it("returns an empty list when the type is an empty object") {
      probationOffenderSearchApiMockServer.stubPostOffenderSearch(
        "{\"pncNumber\": \"$hmppsId\"}",
        """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "contactDetails": {
               "addresses": [
                {
                    "type": {}
                }
              ]
            }
          }
        ]
        """,
      )

      val response = probationOffenderSearchGateway.getAddressesForPerson(hmppsId)

      response.data.first().types.shouldBeEmpty()
    }
  },
)
