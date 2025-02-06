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
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetHmppsIdService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [HmppsIdController::class])
@ActiveProfiles("test")
internal class HmppsIdControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getHmppsIdService: GetHmppsIdService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec({
    val nomisNumber = "A1234AA"
    val path = "/v1/hmpps/id/nomis-number/$nomisNumber"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getHmppsIdService)
        whenever(getHmppsIdService.execute(nomisNumber)).thenReturn(
          Response(
            data = HmppsId(hmppsId = nomisNumber),
          ),
        )
        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("returns a 400 Bad request status code when invalid nomis number provided") {
        whenever(getHmppsIdService.execute(nomisNumber)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)),
          ),
        )
        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
      }

      it("returns a 404 Not found status code when no HMPPS ID found") {
        whenever(getHmppsIdService.execute(nomisNumber)).thenReturn(
          Response(
            data = null,
            errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)),
          ),
        )
        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }

      it("gets the person detail for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getHmppsIdService, VerificationModeFactory.times(1)).execute(nomisNumber)
      }

      it("logs audit") {
        mockMvc.performAuthorised(path)
        verify(
          auditService,
          VerificationModeFactory.times(1),
        ).createEvent(
          "GET_HMPPS_ID_BY_NOMIS_NUMBER",
          mapOf("nomisNumber" to nomisNumber),
        )
      }

      it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

        whenever(getHmppsIdService.execute(nomisNumber)).doThrow(
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
