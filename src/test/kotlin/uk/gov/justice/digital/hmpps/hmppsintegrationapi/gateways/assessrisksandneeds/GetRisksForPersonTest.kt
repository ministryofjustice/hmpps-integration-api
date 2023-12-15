package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AssessRisksAndNeedsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.OtherRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.io.File
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks as IntegrationApiRisks

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetRisksForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) :
  DescribeSpec(
    {
      val assessRisksAndNeedsApiMockServer = AssessRisksAndNeedsApiMockServer()
      val deliusCrn = "X777776"

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubGetRisksForPerson(
          deliusCrn,
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/assessrisksandneeds/fixtures/GetRisksResponse.json").readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRisksForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns risks for the person with the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRisksForPerson(deliusCrn)

        response.data.shouldBe(
          IntegrationApiRisks(
            assessedOn = LocalDateTime.of(2023, 9, 27, 11, 46, 36),
            riskToSelf = RiskToSelf(
              suicide = Risk(
                risk = "Yes",
                previous = "Yes",
                previousConcernsText = "Risk of self harms concerns due to ...",
                current = "Yes",
                currentConcernsText = "Risk of self harms concerns due to ...",
              ),
              selfHarm = Risk(
                risk = "No",
                previous = "No",
                previousConcernsText = "Risk of self harms concerns due to ...",
                current = "No",
                currentConcernsText = "Risk of self harms concerns due to ...",
              ),
              custody = Risk(
                risk = "Don't know",
                previous = "Don't know",
                previousConcernsText = "Risk of self harms concerns due to ...",
                current = "Don't know",
                currentConcernsText = "Risk of self harms concerns due to ...",
              ),
              hostelSetting = Risk(
                risk = "Yes",
                previous = "Yes",
                previousConcernsText = "Risk of self harms concerns due to ...",
                current = "Yes",
                currentConcernsText = "Risk of self harms concerns due to ...",
              ),
              vulnerability = Risk(
                risk = "Yes",
                previous = "Yes",
                previousConcernsText = "Risk of self harms concerns due to ...",
                current = "Yes",
                currentConcernsText = "Risk of self harms concerns due to ...",
              ),
            ),
            otherRisks = OtherRisks(
              escapeOrAbscond = "YES",
              controlIssuesDisruptiveBehaviour = "YES",
              breachOfTrust = "YES",
              riskToOtherPrisoners = "YES",
            ),
            summary = RiskSummary(
              whoIsAtRisk = "X, Y and Z are at risk",
              natureOfRisk = "The nature of the risk is X",
              riskImminence = "the risk is imminent and more probably in X situation",
              riskIncreaseFactors = "If offender in situation X the risk can be higher",
              riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
              overallRiskLevel = "HIGH",
              riskInCommunity = mapOf(
                "children" to "HIGH",
                "public" to "HIGH",
                "knownAdult" to "HIGH",
                "staff" to "MEDIUM",
                "prisoners" to "LOW",
              ),
              riskInCustody = mapOf(
                "children" to "LOW",
                "public" to "LOW",
                "knownAdult" to "HIGH",
                "staff" to "VERY_HIGH",
                "prisoners" to "VERY_HIGH",
              ),
            ),
          ),
        )
      }

      it("returns a 404 NOT FOUND status code when no person is found") {
        assessRisksAndNeedsApiMockServer.stubGetRisksForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRisksForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
