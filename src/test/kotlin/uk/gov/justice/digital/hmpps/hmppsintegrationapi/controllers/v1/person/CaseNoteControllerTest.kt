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
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNoteForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [CaseNoteController::class])
@ActiveProfiles("test")
class CaseNoteControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getCaseNoteForPersonService: GetCaseNoteForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/case-notes"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val pageCaseNote =
      PageCaseNote(
        listOf(
          CaseNote(caseNoteId = "abcd1234"),
        ),
      )
    describe("GET $path") {
      beforeTest {
        Mockito.reset(getCaseNoteForPersonService)
        Mockito.reset(auditService)
        whenever(getCaseNoteForPersonService.execute(hmppsId)).thenReturn(
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

        verify(getCaseNoteForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the case notes for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
          "data":
            {
              "caseNotes": [
                {
               "caseNoteId": "abcd1234"
                }
              ]
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
