package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetAddressesForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nomisGateway: NomisGateway,
) : DescribeSpec({
  val nomisApiMockServer = NomisApiMockServer()
  val offenderNo = "abc123"

  beforeEach {
    nomisApiMockServer.start()
    nomisApiMockServer.stubGetOffenderAddresses(
      offenderNo,
      """
          [
            {
              "postalCode": "SA1 1DP"
            }
          ]
        """,
    )

    Mockito.reset(hmppsAuthGateway)
    whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
  }

  afterTest {
    nomisApiMockServer.stop()
  }

  it("authenticates using HMPPS Auth with credentials") {
    nomisGateway.getAddressesForPerson(offenderNo)

    verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
  }

  it("returns addresses for a person with the matching ID") {
    val response = nomisGateway.getAddressesForPerson(offenderNo)

    response.data.shouldContain(Address(postcode = "SA1 1DP"))
  }

  it("returns an empty list when no addresses are found") {
    nomisApiMockServer.stubGetOffenderAddresses(offenderNo, "[]")

    val response = nomisGateway.getAddressesForPerson(offenderNo)

    response.data.shouldBeEmpty()
  }

  it("returns an error when 404 NOT FOUND is returned") {
    nomisApiMockServer.stubGetOffenderAddresses(
      offenderNo,
      """
        {
          "developerMessage": "cannot find person"
        }
        """,
      HttpStatus.NOT_FOUND,
    )

    val response = nomisGateway.getAddressesForPerson(offenderNo)

    response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
  }
},)
