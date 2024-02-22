package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNotesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [CaseNotesController::class])
@ActiveProfiles("test")
class CaseNotesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getCaseNotesForPersonService: GetCaseNotesForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/case-notes"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val pageCaseNote =

      listOf(
        CaseNote(caseNoteId = "abcd1234"),

      )
    describe("GET $path") {
      beforeTest {
        Mockito.reset(getCaseNotesForPersonService)
        Mockito.reset(auditService)
        whenever(getCaseNotesForPersonService.execute(any())).thenReturn(
          Response(
            data = pageCaseNote,
            errors = emptyList(),
          ),
        )
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the case notes for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getCaseNotesForPersonService, VerificationModeFactory.times(1)).execute(argThat<CaseNoteFilter> { it -> it.hmppsId == hmppsId })
      }

      it("returns the case notes for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

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

      it("logs audit") {
        mockMvc.performAuthorised(path)

        verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_CASE_NOTES", "Person case notes with hmpps id: $hmppsId has been retrieved")
      }
    }
  },
)
