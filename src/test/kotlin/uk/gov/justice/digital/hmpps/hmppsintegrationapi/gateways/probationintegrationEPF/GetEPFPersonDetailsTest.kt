package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationintegrationEPF

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationIntegrationEPFGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateCaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.EffectiveProposalFrameworkAndDeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationIntegrationEPFGateway::class],
)
class GetEPFPersonDetailsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val probationIntegrationEPFGateway: ProbationIntegrationEPFGateway,
) :
  DescribeSpec(
      {
        val effectiveProposalFrameworkAndDeliusMockServer = EffectiveProposalFrameworkAndDeliusMockServer()
        val hmppsId = "X777776"
        val eventNumber = 1234

        beforeEach {
          effectiveProposalFrameworkAndDeliusMockServer.start()
          effectiveProposalFrameworkAndDeliusMockServer.stubGetCaseDetailForPerson(
            hmppsId,
            1234,
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationintegrationEPF/fixtures/GetEPFPersonDetailsResponse.json",
            ).readText(),
          )

          Mockito.reset(hmppsAuthGateway)
          whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
        }

        afterTest {
          effectiveProposalFrameworkAndDeliusMockServer.stop()
        }

        it("authenticates using HMPPS Auth with credentials") {
          probationIntegrationEPFGateway.getCaseDetailForPerson(hmppsId, eventNumber)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
        }

        it("returns case detail for the matching CRN") {
          val response = probationIntegrationEPFGateway.getCaseDetailForPerson(hmppsId, eventNumber)

          response.data.shouldBe(
            generateCaseDetail(),
          )
        }
      },
    )
