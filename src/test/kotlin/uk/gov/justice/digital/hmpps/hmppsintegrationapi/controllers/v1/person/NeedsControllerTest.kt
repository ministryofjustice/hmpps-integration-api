package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetNeedsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@WebMvcTest(controllers = [NeedsController::class])
@ActiveProfiles("test")
internal class NeedsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getNeedsForPersonService: GetNeedsForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "9999/11111A"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val path = "/v1/persons/$encodedHmppsId/needs"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getNeedsForPersonService)
          Mockito.reset(auditService)
          whenever(getNeedsForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                Needs(
                  assessedOn =
                    LocalDateTime.of(
                      2021,
                      5,
                      29,
                      11,
                      21,
                      33,
                    ),
                  identifiedNeeds =
                    listOf(
                      Need(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
                      Need(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
                    ),
                  notIdentifiedNeeds =
                    listOf(
                      Need(type = "RELATIONSHIPS"),
                    ),
                  unansweredNeeds =
                    listOf(
                      Need(type = "LIFESTYLE_AND_ASSOCIATES"),
                      Need(type = "DRUG_MISUSE"),
                      Need(type = "ALCOHOL_MISUSE"),
                    ),
                ),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the needs for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getNeedsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_NEED", mapOf("hmppsId" to hmppsId))
        }

        it("returns the needs for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": {
               "assessedOn": "2021-05-29T11:21:33",
               "identifiedNeeds": [
                  {
                    "type": "EDUCATION_TRAINING_AND_EMPLOYABILITY",
                     "riskOfHarm": null,
                     "riskOfReoffending": null,
                     "severity": null
                  },
                  {
                    "type": "FINANCIAL_MANAGEMENT_AND_INCOME",
                     "riskOfHarm": null,
                     "riskOfReoffending": null,
                     "severity": null
                  }
                ],
                "notIdentifiedNeeds": [
                  {
                    "type": "RELATIONSHIPS",
                     "riskOfHarm": null,
                     "riskOfReoffending": null,
                     "severity": null
                  }
                ],
                "unansweredNeeds": [
                  {
                    "type": "LIFESTYLE_AND_ASSOCIATES",
                    "riskOfHarm": null,
                    "riskOfReoffending": null,
                    "severity": null
                  },
                  {
                    "type": "DRUG_MISUSE",
                     "riskOfHarm": null,
                      "riskOfReoffending": null,
                      "severity": null
                  },
                  {
                    "type": "ALCOHOL_MISUSE",
                     "riskOfHarm": null,
                     "riskOfReoffending": null,
                     "severity": null
                  }
                ]
            }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns null embedded in a JSON object when no needs are found") {
          val hmppsIdForPersonWithNoNeeds = "0000/11111A"
          val encodedHmppsIdForPersonWithNoNeeds =
            URLEncoder.encode(hmppsIdForPersonWithNoNeeds, StandardCharsets.UTF_8)
          val needsPath = "/v1/persons/$encodedHmppsIdForPersonWithNoNeeds/needs"

          whenever(getNeedsForPersonService.execute(hmppsIdForPersonWithNoNeeds)).thenReturn(Response(data = null))

          val result = mockMvc.performAuthorised(needsPath)

          result.response.contentAsString.shouldContain("\"data\":null")
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getNeedsForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getNeedsForPersonService.execute(hmppsId)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised(path)

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }
    },
  )
