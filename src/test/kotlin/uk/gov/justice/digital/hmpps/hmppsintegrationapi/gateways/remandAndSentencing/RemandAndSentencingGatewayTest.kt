package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.remandAndSentencing

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RemandAndSentencingGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.remandAndSentencing.RasSentencedCourtCases
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.objectMapper
import java.io.File
import kotlin.test.Test

class RemandAndSentencingGatewayTest {
  val mockRasRestClient: RestApiClient = mock()
  val mockHmppsAuthGateway: HmppsAuthGateway = mock()
  val remandAndSentencingGateway = RemandAndSentencingGateway(mockRasRestClient, mockHmppsAuthGateway)
  val requestContext = buildRequestContext()
  val rasSentencedCourtCases: RasSentencedCourtCases =
    objectMapper.readValue(
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/remandAndSentencing/fixtures/SentencedCourtCasesResponse.json",
      ).readText(),
      RasSentencedCourtCases::class.java,
    )
  val successResponse = RestApiResponse(UpstreamApi.REMAND_AND_SENTENCING.name, HttpStatus.OK, rasSentencedCourtCases)
  val internalErrorResponse = RestApiResponse<RasSentencedCourtCases>(UpstreamApi.REMAND_AND_SENTENCING.name, HttpStatus.INTERNAL_SERVER_ERROR, null, listOf(RuntimeException("Error")))
  val notFoundResponse = RestApiResponse<RasSentencedCourtCases>(UpstreamApi.REMAND_AND_SENTENCING.name, HttpStatus.INTERNAL_SERVER_ERROR, null, listOf(WebClientResponseException(404, "Not Found", null, null, null, null)))

  @BeforeEach
  fun setup() {
    whenever(mockHmppsAuthGateway.getClientToken(any(), any<RequestContext>())).thenReturn(HmppsAuthMockServer.TOKEN)
    whenever(mockRasRestClient.get(any(), eq(RasSentencedCourtCases::class), any(), isNull())).thenReturn(successResponse)
  }

  @Test
  fun `successfully returns sentenced court cases`() {
    remandAndSentencingGateway.getSentencedCourtCases("A1234BC", requestContext).shouldBe(Response(rasSentencedCourtCases))
  }

  @Test
  fun `returns a 500 error response`() {
    whenever(mockRasRestClient.get(any(), eq(RasSentencedCourtCases::class), any(), isNull())).thenReturn(internalErrorResponse)
    remandAndSentencingGateway.getSentencedCourtCases("A1234BC", requestContext).errors[0] shouldBe UpstreamApiError(UpstreamApi.REMAND_AND_SENTENCING, UpstreamApiError.Type.INTERNAL_SERVER_ERROR, "Error")
  }

  @Test
  fun `returns a 404 error response`() {
    whenever(mockRasRestClient.get(any(), eq(RasSentencedCourtCases::class), any(), isNull())).thenReturn(notFoundResponse)
    remandAndSentencingGateway.getSentencedCourtCases("A1234BC", requestContext).errors[0] shouldBe UpstreamApiError(UpstreamApi.REMAND_AND_SENTENCING, UpstreamApiError.Type.ENTITY_NOT_FOUND, "404 Not Found")
  }
}
