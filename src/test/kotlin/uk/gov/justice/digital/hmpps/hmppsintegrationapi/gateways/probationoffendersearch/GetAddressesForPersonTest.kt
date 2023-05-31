package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class],
)
class GetAddressesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec({
  val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()
  val pncId = "2002/1121M"

  beforeEach {
    probationOffenderSearchApiMockServer.start()
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"pncNumber\": \"$pncId\", \"valid\": true}",
      """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "contactDetails": {
              "addresses": [
                {
                  "id": 123456,
                  "county": "SomeCounty",
                  "from": "2022-08-06",
                  "to": "2022-08-24",
                  "noFixedAbode": false,
                  "notes": "some interesting note",
                  "addressNumber": "123",
                  "streetName": "Some Road",
                  "district": "Handsworth",
                  "town": "Birmingham",
                  "buildingName": "TheBuilding",
                  "postcode": "B11 1CC",
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
    probationOffenderSearchGateway.getAddressesForPerson(pncId)

    verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
  }

  it("returns addresses for a person with the matching ID") {
    val response = probationOffenderSearchGateway.getAddressesForPerson(pncId)

    response.data.shouldContain(
      Address(
        country = "England",
        county = "SomeCounty",
        endDate = "2022-08-24",
        locality = "Handsworth",
        name = "TheBuilding",
        number = "123",
        postcode = "B11 1CC",
        startDate = "2022-08-06",
        street = "Some Road",
        town = "Birmingham",
        type = "Type?",
      ),
    )
  }

  it("returns an empty list when no addresses are found") {
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"pncNumber\": \"$pncId\", \"valid\": true}",
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

    val response = probationOffenderSearchGateway.getAddressesForPerson(pncId)

    response.data.shouldBeEmpty()
  }

  it("returns an error when no results are returned") {
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"pncNumber\": \"$pncId\", \"valid\": true}",
      "[]",
    )

    val response = probationOffenderSearchGateway.getAddressesForPerson(pncId)

    response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
  }
},)
