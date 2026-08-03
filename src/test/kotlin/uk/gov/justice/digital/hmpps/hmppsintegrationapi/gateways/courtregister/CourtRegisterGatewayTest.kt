package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.courtregister

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CourtRegisterGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.objectMapper
import java.io.File
import kotlin.test.Test

class CourtRegisterGatewayTest {
  val mockCourtRegisterRestClient: RestApiClient = mock()
  val mockHmppsAuthGateway: HmppsAuthGateway = mock()
  val courtRegisterGateway = CourtRegisterGateway(mockCourtRegisterRestClient, mockHmppsAuthGateway)
  val requestContext = buildRequestContext()
  val court: Court =
    objectMapper.readValue(
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/courtregister/fixtures/GetCourtResponse.json",
      ).readText(),
      Court::class.java,
    )
  val successResponse = RestApiResponse(UpstreamApi.COURT_REGISTER.name, HttpStatus.OK, court)
  val internalErrorResponse = RestApiResponse<Court>(UpstreamApi.COURT_REGISTER.name, HttpStatus.INTERNAL_SERVER_ERROR, null, listOf(RuntimeException("Error")))
  val notFoundResponse = RestApiResponse<Court>(UpstreamApi.COURT_REGISTER.name, HttpStatus.INTERNAL_SERVER_ERROR, null, listOf(WebClientResponseException(404, "Not Found", null, null, null, null)))

  @BeforeEach
  fun setup() {
    whenever(mockHmppsAuthGateway.getClientToken(any(), any<RequestContext>())).thenReturn(HmppsAuthMockServer.TOKEN)
    whenever(mockCourtRegisterRestClient.get(any(), eq(Court::class), any(), isNull())).thenReturn(successResponse)
  }

  @Test
  fun `successfully returns a court`() {
    courtRegisterGateway.getCourt("ACCRYC", requestContext).shouldBe(Response(court))
  }

  @Test
  fun `returns a 500 error response`() {
    whenever(mockCourtRegisterRestClient.get(any(), eq(Court::class), any(), isNull())).thenReturn(internalErrorResponse)
    courtRegisterGateway.getCourt("ACCRYC", requestContext).errors[0] shouldBe UpstreamApiError(UpstreamApi.COURT_REGISTER, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, "Error")
  }

  @Test
  fun `returns a 404 error response`() {
    whenever(mockCourtRegisterRestClient.get(any(), eq(Court::class), any(), isNull())).thenReturn(notFoundResponse)
    courtRegisterGateway.getCourt("ACCRYC", requestContext).errors[0] shouldBe UpstreamApiError(UpstreamApi.COURT_REGISTER, UpstreamApiError.Type.ENTITY_NOT_FOUND, "404 Not Found")
  }
}
