package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.education

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
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
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.EducationAndWorkPlanGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.education.EducationAssessmentSummaryResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [EducationAndWorkPlanGateway::class],
)
class EducationAndWorkPlanGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val educationGateway: EducationAndWorkPlanGateway,
) : DescribeSpec(
    {
      val id = "123"
      val path = "/assessments/$id/required"
      val educationApiMockServer = ApiMockServer.create(UpstreamApi.EDUCATION_AND_WORK_PLAN)
      beforeEach {
        educationApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("EDUCATION_AND_WORK_PLAN")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }
      afterTest {
        educationApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        educationGateway.getEducationAssessmentSummary(prisonerNumber = "123")

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("EDUCATION_AND_WORK_PLAN")
      }

      it("upstream API returns an error, throw exception") {
        educationApiMockServer.stubForGet(path, "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<WebClientResponseException> {
            educationGateway.getEducationAssessmentSummary(prisonerNumber = "123")
          }
        response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
      }

      it("returns EducationAssessmentSummaryResponse") {
        educationApiMockServer.stubForGet(
          path,
          """
          {
            "basicSkillsAssessmentRequired": true
          }""",
          HttpStatus.OK,
        )
        val response = educationGateway.getEducationAssessmentSummary(prisonerNumber = "123")
        response.data.shouldBe(EducationAssessmentSummaryResponse(true))
      }
    },
  )
