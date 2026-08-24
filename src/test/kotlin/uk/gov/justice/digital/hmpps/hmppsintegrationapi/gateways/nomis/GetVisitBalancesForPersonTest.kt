package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_PRISON_API_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.visits.VisitBalances

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetVisitBalancesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec({
    val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
    val offenderNumber = "G2996UX"
    val visitBalancesPath = "/api/bookings/offenderNo/$offenderNumber/visit/balances"
    val responseJson =
      """
        {
          "remainingVo": 1073741824,
          "remainingPvo": 1073741824,
          "latestIepAdjustDate": "2025-03-04",
          "latestPrivIepAdjustDate": "2025-03-04"
        }
        """.removeWhitespaceAndNewlines()

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubForGet(
        visitBalancesPath,
        responseJson,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      prisonApiGateway.getVisitBalances(offenderNumber)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns visit balances for the matching offender number") {
      val response = prisonApiGateway.getVisitBalances(offenderNumber)

      response.data.shouldNotBeNull()
      response.data?.remainingVo.shouldBe(1073741824)
    }

    it("returns an error when 400 Bad Request is returned because of an invalid request") {
      nomisApiMockServer.stubForGet(visitBalancesPath, "", HttpStatus.BAD_REQUEST)

      val response = prisonApiGateway.getVisitBalances(offenderNumber)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.PRISON_API)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
    }

    it("returns an error when 404 Not Found is returned because no person is found") {
      nomisApiMockServer.stubForGet(visitBalancesPath, "", HttpStatus.NOT_FOUND)

      val response = prisonApiGateway.getVisitBalances(offenderNumber)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.PRISON_API)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }

    it("can use the RestApiClient") {
      // Given
      val authToken = "ABC123"
      val headers = mapOf("Authorization" to "Bearer $authToken")

      val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

      val authGateway: HmppsAuthGateway = mock()
      whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

      val apiClient: RestApiClient = mock()
      whenever(apiClient.get(eq(visitBalancesPath), eq(VisitBalances::class), eq(headers), isNull())).thenReturn(
        RestApiResponse(
          "Test",
          HttpStatus.OK,
          RestApiClient.mapResponse(responseJson, VisitBalances::class),
        ),
      )

      val gateway = PrisonApiGateway("http://localhost", features, apiClient)
      gateway.hmppsAuthGateway = authGateway

      // When
      val response = gateway.getVisitBalances(offenderNumber)

      // Then
      response.data.shouldNotBeNull()
      response.data?.remainingVo.shouldBe(1073741824)
    }
  })
