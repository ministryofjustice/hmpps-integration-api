package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskAssessment
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskCategoriesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [RiskCategoriesController::class])
@ActiveProfiles("test")
internal class RiskCategoriesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getRiskCategoriesForPersonService: GetRiskCategoriesForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "AA11111A"
      val path = "/v1/persons/$hmppsId/risks/categories"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getRiskCategoriesForPersonService)
          whenever(getRiskCategoriesForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                RiskCategory(
                  offenderNo = "A1234AA",
                  assessments =
                    listOf(
                      RiskAssessment(
                        classificationCode = "C",
                        classification = "Cat C",
                        assessmentCode = "CATEGORY",
                        assessmentDescription = "Categorisation",
                        assessmentDate = "2018-02-11",
                        nextReviewDate = "2018-02-11",
                        assessmentAgencyId = "MDI",
                        assessmentStatus = "P",
                        assessmentComment = "Comment details",
                      ),
                    ),
                  category = "string",
                  categoryCode = "string",
                ),
            ),
          )

          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the risk categories for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(getRiskCategoriesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_RISK_CATEGORIES", mapOf("hmppsId" to hmppsId))
        }

        it("returns the risk categories for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": {
            "offenderNo": "A1234AA",
            "assessments": [
            {
              "classificationCode": "C",
              "classification": "Cat C",
              "assessmentCode": "CATEGORY",
              "assessmentDescription": "Categorisation",
              "assessmentDate": "2018-02-11",
              "nextReviewDate": "2018-02-11",
              "assessmentAgencyId": "MDI",
              "assessmentStatus": "P",
              "assessmentComment": "Comment details"
            }
          ],
            "category": "string",
            "categoryCode": "string"
          }
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getRiskCategoriesForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = RiskCategory(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 404 NOT FOUND status code consumer not in allowed for endpoint") {
          whenever(getRiskCategoriesForPersonService.execute(hmppsId, ConsumerFilters(prisons = listOf("XYZ")))).thenReturn(
            Response(
              data = RiskCategory(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorisedWithCN(path, "limited-prisons")

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }
      }
    },
  )
