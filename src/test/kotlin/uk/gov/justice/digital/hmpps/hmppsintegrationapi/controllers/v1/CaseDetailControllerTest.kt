package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseDetailService

@WebMvcTest(controllers = [CaseDetailController::class])
internal class CaseDetailControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getCaseDetailService: GetCaseDetailService,
) : DescribeSpec({
  val hmppsId = "X12345"
  val eventNumber = 1234
  val path = "/v1/case-details/$hmppsId/$eventNumber"

  describe("GET $path") {
    beforeTest {
      Mockito.reset(getCaseDetailService)
      whenever(getCaseDetailService.execute(hmppsId, eventNumber)).thenReturn(
        Response(
          data = CaseDetail(nomsId = "ABC123"),
        ),
      )
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the case detail for a person with the matching ID") {
      mockMvc.perform(MockMvcRequestBuilders.get("$path")).andReturn()

      verify(getCaseDetailService, VerificationModeFactory.times(1)).execute(hmppsId, eventNumber)
    }
  }
},)
