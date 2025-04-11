package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.FeatureNotEnabledException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OtherRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetRisksForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/risks/rosh/$deliusCrn"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(featureFlag.useArnsEndpoints).thenReturn(true)
        assessRisksAndNeedsApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/assessrisksandneeds/fixtures/GetRisksResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns risks for the person with the matching CRN") {
        val response = assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)

        response.data.shouldBe(
          Risks(
            assessedOn = LocalDateTime.of(2023, 9, 27, 11, 46, 36),
            riskToSelf =
              RiskToSelf(
                suicide =
                  Risk(
                    risk = "Yes",
                    previous = "Yes",
                    previousConcernsText = "Risk of self harms concerns due to ...",
                    current = "Yes",
                    currentConcernsText = "Risk of self harms concerns due to ...",
                  ),
                selfHarm =
                  Risk(
                    risk = "No",
                    previous = "No",
                    previousConcernsText = "Risk of self harms concerns due to ...",
                    current = "No",
                    currentConcernsText = "Risk of self harms concerns due to ...",
                  ),
                custody =
                  Risk(
                    risk = "Don't know",
                    previous = "Don't know",
                    previousConcernsText = "Risk of self harms concerns due to ...",
                    current = "Don't know",
                    currentConcernsText = "Risk of self harms concerns due to ...",
                  ),
                hostelSetting =
                  Risk(
                    risk = "Yes",
                    previous = "Yes",
                    previousConcernsText = "Risk of self harms concerns due to ...",
                    current = "Yes",
                    currentConcernsText = "Risk of self harms concerns due to ...",
                  ),
                vulnerability =
                  Risk(
                    risk = "Yes",
                    previous = "Yes",
                    previousConcernsText = "Risk of self harms concerns due to ...",
                    current = "Yes",
                    currentConcernsText = "Risk of self harms concerns due to ...",
                  ),
              ),
            otherRisks =
              OtherRisks(
                escapeOrAbscond = "YES",
                controlIssuesDisruptiveBehaviour = "YES",
                breachOfTrust = "YES",
                riskToOtherPrisoners = "YES",
              ),
            summary =
              RiskSummary(
                whoIsAtRisk = "X, Y and Z are at risk",
                natureOfRisk = "The nature of the risk is X",
                riskImminence = "the risk is imminent and more probably in X situation",
                riskIncreaseFactors = "If offender in situation X the risk can be higher",
                riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
                overallRiskLevel = "HIGH",
                riskInCommunity =
                  mapOf(
                    "children" to "HIGH",
                    "public" to "HIGH",
                    "knownAdult" to "HIGH",
                    "staff" to "MEDIUM",
                    "prisoners" to "LOW",
                  ),
                riskInCustody =
                  mapOf(
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
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("returns 503 service not available when feature flag set to false") {
        whenever(featureFlag.useArnsEndpoints).thenReturn(false)
        val exception = shouldThrow<FeatureNotEnabledException> { assessRisksAndNeedsGateway.getRiskSeriousHarmForPerson(deliusCrn) }
        exception.message.shouldContain("use-arns-endpoints not enabled")
      }
    },
  )
