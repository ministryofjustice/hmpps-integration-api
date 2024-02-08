package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNoteForPersonService

@WebMvcTest(controllers = [EPFPersonDetailController::class])
@ActiveProfiles("test")
class CaseNoteControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val getCaseNoteForPersonService: GetCaseNoteForPersonService,
) : DescribeSpec({
    val hmppsId = "X12345"
    val path = "/v1/caseNotes/$hmppsId"
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
        whenever(getCaseNoteForPersonService.execute(hmppsId)).thenReturn(
          Response(data = pageCaseNote, errors = listOf()),
        )
      }
    }

    it("returns a 200 OK status code") {
      val result = mockMvc.performAuthorised(path)
      result.response.status.shouldBe(HttpStatus.OK.value())
    }
  })
