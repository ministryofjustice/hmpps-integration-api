package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetDynamicRisksForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/case/$deliusCrn/supervisions"
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubForGet(
          path,
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
        nDeliusGateway.getDynamicRisksForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns dynamic risks for the matching CRN") {
        val response = nDeliusGateway.getDynamicRisksForPerson(deliusCrn)

        response.data.shouldBe(
          listOf(
            DynamicRisk(
              code = "RCCO",
              description = "Child protection issues",
              startDate = "2018-07-27",
              reviewDate = "2026-03-20",
              notes = "These notes are about the person.",
            ),
          ),
        )
      }

      it("returns an empty list if no dynamic risks are found") {
        nDeliusApiMockServer.stubForGet(
          path,
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

        val response = nDeliusGateway.getDynamicRisksForPerson(deliusCrn)

        response.data.shouldBe(emptyList())
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nDeliusApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = nDeliusGateway.getDynamicRisksForPerson(deliusCrn)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NDELIUS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
