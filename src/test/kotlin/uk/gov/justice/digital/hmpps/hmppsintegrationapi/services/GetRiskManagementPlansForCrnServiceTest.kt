package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RiskManagementGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.CrnRiskManagementPlan
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.CrnRiskManagementPlans
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetRiskManagementPlansForCrnService::class],
)
class GetRiskManagementPlansForCrnServiceTest(
  @MockBean val riskManagementGateway: RiskManagementGateway,
  private val serviceUnderTest: GetRiskManagementPlansForCrnService,
) : DescribeSpec({

    val crn = "D1974X"
    val badCrn = "Not a real CRN"
    val testPlan =
      CrnRiskManagementPlans(
        crn = crn,
        limitedAccessOffender = "true",
        riskManagementPlan =
          listOf(
            CrnRiskManagementPlan(
              assessmentId = "123450",
              dateCompleted = "2024-05-04T01 =04 =20",
              partcompStatus = "string",
              initiationDate = "2024-05-04T01 =04 =20",
              assessmentStatus = "string",
              assessmentType = "string",
              superStatus = "string",
              keyInformationCurrentSituation = "string",
              furtherConsiderationsCurrentSituation = "string",
              supervision = "string",
              monitoringAndControl = "string",
              interventionsAndTreatment = "string",
              victimSafetyPlanning = "string",
              contingencyPlans = "string",
              laterWIPAssessmentExists = "true",
              latestWIPDate = "2024-05-04T01 =04 =20",
              laterSignLockAssessmentExists = "true",
              latestSignLockDate = "2024-05-04T01 =04 =20",
              laterPartCompUnsignedAssessmentExists = "true",
              latestPartCompUnsignedDate = "2024-05-04T01 =04 =20",
              laterPartCompSignedAssessmentExists = "true",
              latestPartCompSignedDate = "2024-05-04T01 =04 =20",
              laterCompleteAssessmentExists = "true",
              latestCompleteDate = "2024-05-04T01 =04 =20",
            ),
            CrnRiskManagementPlan(
              assessmentId = "123451",
              dateCompleted = "2024-05-04T01 =04 =20",
              partcompStatus = "string",
              initiationDate = "2024-05-04T01 =04 =20",
              assessmentStatus = "string",
              assessmentType = "string",
              superStatus = "string",
              keyInformationCurrentSituation = "string",
              furtherConsiderationsCurrentSituation = "string",
              supervision = "string",
              monitoringAndControl = "string",
              interventionsAndTreatment = "string",
              victimSafetyPlanning = "string",
              contingencyPlans = "string",
              laterWIPAssessmentExists = "true",
              latestWIPDate = "2024-05-04T01 =04 =20",
              laterSignLockAssessmentExists = "true",
              latestSignLockDate = "2024-05-04T01 =04 =20",
              laterPartCompUnsignedAssessmentExists = "true",
              latestPartCompUnsignedDate = "2024-05-04T01 =04 =20",
              laterPartCompSignedAssessmentExists = "true",
              latestPartCompSignedDate = "2024-05-04T01 =04 =20",
              laterCompleteAssessmentExists = "true",
              latestCompleteDate = "2024-05-04T01 =04 =20",
            ),
          ),
      )
    val testErrors = listOf(
      UpstreamApiError(
        causedBy = UpstreamApi.RISK_MANAGEMENT_PLAN,
        type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        description = "Lorem Ipsum dolor sit amet",
      )
    )

    beforeEach {
      Mockito.reset(riskManagementGateway)

      whenever(riskManagementGateway.getRiskManagementPlansForCrn(crn)).thenReturn(
        Response(data = testPlan, errors = emptyList()),
      )
      whenever(riskManagementGateway.getRiskManagementPlansForCrn(badCrn)).thenReturn(
        Response(data = null, errors = testErrors),
      )
    }

    describe("Get risk management") {

      it("returns plans with valid CRN") {
        val result = serviceUnderTest.execute(crn)
        verify(riskManagementGateway, VerificationModeFactory.times(1)).getRiskManagementPlansForCrn(crn)
        assert(result.data?.size == 2)
      }

      it("Returns error without valid CRN") {
        val result = serviceUnderTest.execute(badCrn)
        verify(riskManagementGateway, VerificationModeFactory.times(1)).getRiskManagementPlansForCrn(badCrn)
        result.data?.isEmpty()?.let { assert(it) }
        assert(result.errors.isNotEmpty())
      }
    }
  })
