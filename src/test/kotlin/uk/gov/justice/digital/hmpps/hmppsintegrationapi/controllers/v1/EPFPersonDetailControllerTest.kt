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
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetEPFPersonDetailService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [EPFPersonDetailController::class])
@ActiveProfiles("test")
internal class EPFPersonDetailControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getEPFPersonDetailService: GetEPFPersonDetailService,
  @MockBean val auditService: AuditService,
) : DescribeSpec({
  val hmppsId = "X12345"
  val eventNumber = 1234
  val path = "/v1/epf/person-details/$hmppsId/$eventNumber"
  val mockMvc = IntegrationAPIMockMvc(springMockMvc)

  describe("GET $path") {
    beforeTest {
      Mockito.reset(getEPFPersonDetailService)
      whenever(getEPFPersonDetailService.execute(hmppsId, eventNumber)).thenReturn(
        Response(
          data = CaseDetail(nomsId = "ABC123"),
        ),
      )
      Mockito.reset(auditService)
    }

    it("responds with a 200 OK status") {
      val result = mockMvc.performAuthorised(path)

      result.response.status.shouldBe(HttpStatus.OK.value())
    }

    it("retrieves the person detail for a person with the matching ID") {
      mockMvc.performAuthorised(path)

      verify(getEPFPersonDetailService, VerificationModeFactory.times(1)).execute(hmppsId, eventNumber)
    }

    it("logs audit") {
      mockMvc.performAuthorised(path)
      verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_EPF_PROBATION_CASE_INFORMATION", "Probation case information with hmpps Id: $hmppsId and delius event number: $eventNumber has been retrieved")
    }
  }
},)
