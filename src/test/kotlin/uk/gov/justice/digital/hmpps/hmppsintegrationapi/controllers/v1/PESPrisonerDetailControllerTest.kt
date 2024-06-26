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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonIntegrationpes.PESPrisonerDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPESPrisonerDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [PESPrisonerDetailController::class])
@ActiveProfiles("test")
internal class PESPrisonerDetailControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getPESPrisonerDetailsService: GetPESPrisonerDetailsService,
  @MockBean val auditService: AuditService,
) : DescribeSpec({
    val hmppsId = "X12345"
    val path = "/v1/pes/prisoner-details/$hmppsId"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getPESPrisonerDetailsService)
        whenever(getPESPrisonerDetailsService.execute(hmppsId)).thenReturn(
          Response(
            data = PESPrisonerDetails(prisonerNumber = "ABC123", "Molly", lastName = "Mob", prisonId = "LEI", prisonName = "HMP Leeds", cellLocation = "6-2-006"),
          ),
        )
        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the prisoner detail for a prisoner with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getPESPrisonerDetailsService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("logs audit") {
        mockMvc.performAuthorised(path)
        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent(
          "GET_PES_PRISONER_INFORMATION",
          mapOf("hmppsId" to hmppsId),
        )
      }

      it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

        whenever(getPESPrisonerDetailsService.execute(hmppsId)).doThrow(
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
