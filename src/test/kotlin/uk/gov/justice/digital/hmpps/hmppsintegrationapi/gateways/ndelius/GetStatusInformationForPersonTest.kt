package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NDeliusApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.StatusInformation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetStatusInformationForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) :
  DescribeSpec(
      {
        val nDeliusApiMockServer = NDeliusApiMockServer()
        val deliusCrn = "X777776"

        beforeEach {
          nDeliusApiMockServer.start()
          nDeliusApiMockServer.stubGetSupervisionsForPerson(
            deliusCrn,
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetSupervisionsResponse.json",
            ).readText(),
          )

          Mockito.reset(hmppsAuthGateway)
          whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
        }

        afterTest {
          nDeliusApiMockServer.stop()
        }

        it("authenticates using HMPPS Auth with credentials") {
          nDeliusGateway.getStatusInformationForPerson(deliusCrn)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
        }

        it("returns status information for the matching CRN") {
          val response = nDeliusGateway.getStatusInformationForPerson(deliusCrn)

          response.data.shouldBe(
            listOf(
              StatusInformation(
                code = "ASFO",
                description = "Serious Further Offence - Subject to SFO review/investigation",
                startDate = "2020-12-04",
                reviewDate = "2030-07-25",
                notes = "No notes.",
              ),
            ),
          )
        }

        it("returns an empty list if no status information is found") {
          nDeliusApiMockServer.stubGetSupervisionsForPerson(
            deliusCrn,
            """
            {
              "communityManager": {},
              "mappaDetail": {},
              "supervisions": [],
              "dynamicRisks": [],
              "personStatus": []
            }
          """,
          )

          val response = nDeliusGateway.getStatusInformationForPerson(deliusCrn)

          response.data.shouldBe(emptyList())
        }

        it("returns an error when 404 Not Found is returned because no person is found") {
          nDeliusApiMockServer.stubGetSupervisionsForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

          val response = nDeliusGateway.getStatusInformationForPerson(deliusCrn)

          response.errors.shouldHaveSize(1)
          response.errors.first().causedBy.shouldBe(UpstreamApi.NDELIUS)
          response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }
      },
    )
