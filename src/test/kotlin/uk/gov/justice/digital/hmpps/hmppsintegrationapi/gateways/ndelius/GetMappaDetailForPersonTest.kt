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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetMappaDetailForPersonTest(
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
          nDeliusGateway.getMappaDetailForPerson(deliusCrn)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
        }

        it("returns Mappa detail for the matching CRN") {
          val response = nDeliusGateway.getMappaDetailForPerson(deliusCrn)

          response.data.shouldBe(
            MappaDetail(
              level = 1,
              levelDescription = "string",
              category = 1,
              categoryDescription = "string",
              startDate = "string",
              reviewDate = "string",
              notes = "string",
            ),
          )
        }

        it("returns an empty list if no mappa detail is found") {
          nDeliusApiMockServer.stubGetSupervisionsForPerson(
            deliusCrn,
            """
            {
              "mappaDetail": {},
              "supervisions": []
            }
          """,
          )

          val response = nDeliusGateway.getMappaDetailForPerson(deliusCrn)

          response.data.shouldBe(MappaDetail())
        }

        it("returns an error when 404 Not Found is returned because no person is found") {
          nDeliusApiMockServer.stubGetSupervisionsForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

          val response = nDeliusGateway.getMappaDetailForPerson(deliusCrn)

          response.errors.shouldHaveSize(1)
          response.errors.first().causedBy.shouldBe(UpstreamApi.NDELIUS)
          response.errors.first().type.shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
        }
      },
    )
