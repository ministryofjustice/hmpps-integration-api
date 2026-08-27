package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiScheduledEvents
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetScheduledMovementsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val nomisNumber = "A8451DY"
      val scheduledMovementsPath = "/api/offenders/$nomisNumber/scheduled-events"
      val authToken = "ABC123"
      val features = FeatureFlagConfig()
      val responseJson =
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/GetScheduledMovementsResponse.json",
        ).readText()
      val requestContext = buildRequestContext("testUser")

      beforeEach {
        nomisApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("returns scheduled movements for the matching person ID") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.getList(eq(scheduledMovementsPath), eq(PrisonApiScheduledEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapListResponse(responseJson, PrisonApiScheduledEvents::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getScheduledMovements(nomisNumber, requestContext)

        response.errors.shouldBeEmpty()
        response.data[0].startTime.shouldBe("2026-08-27T00:33:28.896Z")
        response.data[0].eventSubType.shouldBe("PA")
        response.data[0].outcomeComment.shouldBe("Comment")

        response.data[1].startTime.shouldBe("2026-08-27T00:33:28.896Z")
        response.data[1].eventSubType.shouldBe("PA")
        response.data[1].outcomeComment.shouldBe("Comment")
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.getList(eq(scheduledMovementsPath), eq(PrisonApiScheduledEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.NOT_FOUND,
            errors = listOf(WebClientResponseException(404, "Not Found", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getScheduledMovements(nomisNumber, requestContext)

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

      it("returns an error when 400 Bad Request is returned because of invalid ID") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.getList(eq(scheduledMovementsPath), eq(PrisonApiScheduledEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.BAD_REQUEST,
            errors = listOf(WebClientResponseException(400, "Bad Request", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getScheduledMovements(nomisNumber, requestContext)

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
    },
  )
