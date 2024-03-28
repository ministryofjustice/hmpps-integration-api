package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
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

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the person detail for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getEPFPersonDetailService, VerificationModeFactory.times(1)).execute(hmppsId, eventNumber)
      }

      it("logs audit") {
        mockMvc.performAuthorised(path)
        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent(
          "GET_EPF_PROBATION_CASE_INFORMATION",
          "Probation case information with hmpps Id: $hmppsId and delius event number: $eventNumber has been retrieved",
        )
      }

      it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

        whenever(getEPFPersonDetailService.execute(hmppsId, eventNumber)).doThrow(
          WebClientResponseException(500, "MockError", null, null, null, null),
        )

        val result = mockMvc.performAuthorised(path)
        assert(result.response.status == 500)
        assert(
          result.response.contentAsString.equals(
            "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
          ),
        )
      }
    }
  })
