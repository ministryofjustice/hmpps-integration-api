package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ProbationOffenderSearchApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class]
)
class GetAddressesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway
) : DescribeSpec({
  val probationOffenderSearchApiMockServer = ProbationOffenderSearchApiMockServer()
  val nomsNumber = "qwe678"

  beforeEach {
    probationOffenderSearchApiMockServer.start()
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"nomsNumber\": \"$nomsNumber\", \"valid\": true}",
      """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "otherIds": {
              "nomsNumber": "$nomsNumber"
            },
            "contactDetails": {
              "addresses": [
                {
                 "postcode": "M3 2JA"
                }
              ]
            }
          }
        ]
      """
    )

    Mockito.reset(hmppsAuthGateway)
    whenever(hmppsAuthGateway.getClientToken("Probation Offender Search")).thenReturn(
      HmppsAuthMockServer.TOKEN
    )
  }

  afterTest {
    probationOffenderSearchApiMockServer.stop()
  }

  it("authenticates using HMPPS Auth with credentials") {
    probationOffenderSearchGateway.getAddressesForPerson(nomsNumber)

    verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search")
  }

  it("returns addresses for a person with the matching ID") {
    val addresses = probationOffenderSearchGateway.getAddressesForPerson(nomsNumber)

    addresses.shouldContain(Address(postcode = "M3 2JA"))
  }

  it("returns an empty list when no addresses are found") {
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"nomsNumber\": \"$nomsNumber\", \"valid\": true}",
      """
        [
          {
            "firstName": "English",
            "surname": "Breakfast",
            "otherIds": {
              "nomsNumber": "$nomsNumber"
            },
            "contactDetails": {
              "addresses": []
            }
          }
        ]
        """
    )

    val addresses = probationOffenderSearchGateway.getAddressesForPerson(nomsNumber)

    addresses.shouldBeEmpty()
  }

  it("throws an exception when no results are returned") {
    probationOffenderSearchApiMockServer.stubPostOffenderSearch(
      "{\"nomsNumber\": \"$nomsNumber\", \"valid\": true}",
      "[]"
    )

    val exception = shouldThrow<EntityNotFoundException> {
      probationOffenderSearchGateway.getAddressesForPerson(nomsNumber)
    }

    exception.message.shouldBe("Could not find person with id: $nomsNumber")
  }
})
