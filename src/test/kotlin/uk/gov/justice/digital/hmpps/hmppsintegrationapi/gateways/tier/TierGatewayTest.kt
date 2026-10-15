package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.tier

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.TierGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier.TierV3DetailsDto
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class TierGatewayTest {
  val hmppsAuthGateway: HmppsAuthGateway = mock()
  val apiClient: RestApiClient = mock()
  val authToken = "ABC123"
  val headers = mapOf("Authorization" to "Bearer $authToken")
  val requestContext = buildRequestContext()
  lateinit var tierGateway: TierGateway
  val responseJson =
    File(
      "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/tier/fixtures/TierDetailsResponse.json",
    ).readText()

  @BeforeEach
  fun setup() {
    whenever(hmppsAuthGateway.getClientToken("TIER", requestContext)).thenReturn(authToken)
    tierGateway = TierGateway(apiClient, hmppsAuthGateway)
  }

  @Test
  fun `successfully returns the tier calculation from upstream`() {
    whenever(apiClient.get(any(), eq(TierV3DetailsDto::class), eq(headers), isNull()))
      .thenReturn(RestApiResponse("TIER", HttpStatus.OK, RestApiClient.mapResponse(responseJson, TierV3DetailsDto::class)))
    val response = tierGateway.getTierDetails("A123456", requestContext)
    assertEquals("123e4567-e89b-12d3-a456-426614174000", response.data?.calculationId)
  }

  @Test
  fun `returns an error response when fetching tier details`() {
    whenever(apiClient.get(any(), eq(TierV3DetailsDto::class), eq(headers), isNull()))
      .thenReturn(
        RestApiResponse(
          "Test",
          HttpStatus.NOT_FOUND,
          null,
          listOf(WebClientResponseException(404, "Tier not found", null, null, null)),
        ),
      )
    val response = tierGateway.getTierDetails("A123456", requestContext)
    response shouldNotBe null
    response.errors.size shouldBe 1
    response.errors[0].description shouldBe "404 Tier not found"
  }
}
