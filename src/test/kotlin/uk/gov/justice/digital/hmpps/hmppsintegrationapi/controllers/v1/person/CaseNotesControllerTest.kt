package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.HmppsIdConverter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNotesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDateTime

@WebMvcTest(controllers = [CaseNotesController::class])
@ActiveProfiles("test")
class CaseNotesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getCaseNotesForPersonService: GetCaseNotesForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val hmppsIdConverter: HmppsIdConverter,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "G2996UX"
      val locationId = "MDI"
      val startDate: LocalDateTime = LocalDateTime.now()
      val endDate: LocalDateTime = LocalDateTime.now()
      val path = "/v1/persons/$hmppsId/case-notes?startDate=$startDate&endDate=$endDate&locationId=$locationId"
      val caseNoteFilter = CaseNoteFilter(hmppsId, startDate, endDate, locationId)
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val pageCaseNote =
        listOf(
          CaseNote(caseNoteId = "abcd1234"),
        )
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getCaseNotesForPersonService)
          Mockito.reset(auditService)

          whenever(getCaseNotesForPersonService.execute(caseNoteFilter, filters)).thenReturn(
            Response(
              data = pageCaseNote,
              errors = emptyList(),
            ),
          )
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            times(1),
          ).createEvent("GET_CASE_NOTES", mapOf("hmppsId" to hmppsId))
        }

        it("passes filters into service") {
          mockMvc.performAuthorisedWithCN(path, "limited-prisons")

          verify(
            getCaseNotesForPersonService,
            times(1),
          ).execute(
            caseNoteFilter,
            ConsumerFilters(prisons = listOf("XYZ")),
          )
        }

        it("returns the case notes for a person with the matching ID with a 200 status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsString.shouldContain(
            """
            {
              "data": [
                {
                  "caseNoteId": "abcd1234",
                  "offenderIdentifier": null,
                  "type": null,
                  "typeDescription": null,
                  "subType": null,
                  "subTypeDescription": null,
                  "creationDateTime": null,
                  "occurrenceDateTime": null,
                  "text": null,
                  "locationId": null,
                  "sensitive": false,
                  "amendments": []
                }
              ],
              "pagination": {
                "isLastPage": true,
                "count": 1,
                "page": 1,
                "perPage": 10,
                "totalCount": 1,
                "totalPages": 1
              }
            }
            """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 400 when the upstream service returns bad request") {
          whenever(getCaseNotesForPersonService.execute(caseNoteFilter, filters)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.BAD_REQUEST,
                    causedBy = UpstreamApi.NOMIS,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("returns a 403 when the upstream service provides a 403") {
          whenever(getCaseNotesForPersonService.execute(caseNoteFilter, filters)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.FORBIDDEN,
                    causedBy = UpstreamApi.CASE_NOTES,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.FORBIDDEN.value())
        }

        it("returns a 400 when the upstream service returns entity not found") {
          whenever(getCaseNotesForPersonService.execute(caseNoteFilter, filters)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    causedBy = UpstreamApi.NOMIS,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getCaseNotesForPersonService.execute(caseNoteFilter, filters)).doThrow(
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
