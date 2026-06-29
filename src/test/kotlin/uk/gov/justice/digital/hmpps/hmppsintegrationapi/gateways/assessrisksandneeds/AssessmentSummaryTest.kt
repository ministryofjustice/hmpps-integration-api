package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class AssessmentSummaryTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/assessment-summary/$deliusCrn"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/assessrisksandneeds/fixtures/AssessmentSummaryResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("returns an assessment summary") {
        val summary = assessRisksAndNeedsGateway.getAssessmentSummary(deliusCrn)
        summary.data.shouldBe(
          AssessmentSummary(
            initiationDate = LocalDateTime.of(2026, 1, 16, 16, 22, 54),
            completedDate = LocalDateTime.of(2026, 2, 5, 9, 13, 21),
            assessmentType = "Test Assessment Type",
            status = "Test Assessment Status",
            assessorName = "Test Assessor Name",
            countersignerName = "Test Countersigner Name",
          ),
        )
      }

      it("returns a stubbed assessment summary") {
        whenever(featureFlag.isEnabled(FeatureFlagConfig.USE_STUBBED_ASSESSMENT_SUMMARY)).thenReturn(true)
        val summary = assessRisksAndNeedsGateway.getAssessmentSummary(deliusCrn)
        summary.data.shouldBe(
          AssessmentSummary(
            initiationDate = LocalDateTime.of(2026, 1, 5, 11, 28, 32),
            completedDate = LocalDateTime.of(2026, 2, 22, 15, 9, 3),
            assessmentType = "Stubbed Assessment Type",
            status = "Stubbed Assessment Status",
            assessorName = "Stubbed Assessor Name",
            countersignerName = "Stubbed Countersigner Name",
          ),
        )
      }

      it("returns a not found error") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)
        val response = assessRisksAndNeedsGateway.getAssessmentSummary(deliusCrn)
        response.errors.shouldContain(UpstreamApiError(UpstreamApi.ASSESS_RISKS_AND_NEEDS, UpstreamApiError.Type.ENTITY_NOT_FOUND))
      }

      it("returns a forbidden error") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.FORBIDDEN)
        val response = assessRisksAndNeedsGateway.getAssessmentSummary(deliusCrn)
        response.errors.shouldContain(UpstreamApiError(UpstreamApi.ASSESS_RISKS_AND_NEEDS, UpstreamApiError.Type.FORBIDDEN))
      }
    },
  )
