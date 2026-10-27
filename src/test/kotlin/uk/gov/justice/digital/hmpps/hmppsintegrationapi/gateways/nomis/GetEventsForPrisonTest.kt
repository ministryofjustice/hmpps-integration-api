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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiEvents
import java.io.File
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetEventsForPrisonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val prisonId = "BMI"
      val nomisId = "A1417AE"
      val eventType = "ALERT"
      val limit = 100
      val fromDateTime = LocalDateTime.now().toString()
      val eventsShortPath = "/api/v1/offenders/events?prison_id=$prisonId&from_datetime=$fromDateTime"
      val eventsLongPath = "/api/v1/offenders/events?prison_id=$prisonId&from_datetime=$fromDateTime&offender_id=$nomisId&event_type=$eventType&limit=$limit"
      val authToken = "ABC123"
      val features = FeatureFlagConfig()
      val responseJson =
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/GetEventsResponse.json",
        ).readText()
      val requestContext = buildRequestContext("testUser")

      beforeEach {
        nomisApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("returns events for the matching prison ID with no extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsShortPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiEvents::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, null, null, fromDateTime, null, requestContext)

        response.errors.shouldBeEmpty()
        response.data?.events?.shouldHaveSize(1)
        response.data
          ?.events[0]
          ?.type
          .shouldBe("IEP_CHANGED")
        response.data
          ?.events[0]
          ?.id
          .shouldBe(21)
        response.data
          ?.events[0]
          ?.noms_id
          .shouldBe("A1417AE")
        response.data
          ?.events[0]
          ?.prison_id
          .shouldBe("BMI")
        response.data
          ?.events[0]
          ?.timestamp
          .shouldBe("2016-10-21 15:55:06.284")
        response.data
          ?.events[0]
          ?.eventData
          .shouldBe("string")
      }

      it("returns an error when 404 Not Found is returned because no events are found with no extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsShortPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.NOT_FOUND,
            errors = listOf(WebClientResponseException(404, "Not Found", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, null, null, fromDateTime, null, requestContext)

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

      it("returns an error when 400 Bad Request is returned because of invalid ID with no extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsShortPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.BAD_REQUEST,
            errors = listOf(WebClientResponseException(400, "Bad Request", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, null, null, fromDateTime, null, requestContext)

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

      it("returns events for the matching prison ID with extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsLongPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiEvents::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, nomisId, eventType, fromDateTime, limit, requestContext)

        response.errors.shouldBeEmpty()
        response.data?.events?.shouldHaveSize(1)
        response.data
          ?.events[0]
          ?.type
          .shouldBe("IEP_CHANGED")
        response.data
          ?.events[0]
          ?.id
          .shouldBe(21)
        response.data
          ?.events[0]
          ?.noms_id
          .shouldBe("A1417AE")
        response.data
          ?.events[0]
          ?.prison_id
          .shouldBe("BMI")
        response.data
          ?.events[0]
          ?.timestamp
          .shouldBe("2016-10-21 15:55:06.284")
        response.data
          ?.events[0]
          ?.eventData
          .shouldBe("string")
      }

      it("returns an error when 404 Not Found is returned because no events are found with extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsLongPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.NOT_FOUND,
            errors = listOf(WebClientResponseException(404, "Not Found", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, nomisId, eventType, fromDateTime, limit, requestContext)

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

      it("returns an error when 400 Bad Request is returned because of invalid ID with extra optional parameters") {
        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(eventsLongPath), eq(PrisonApiEvents::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.BAD_REQUEST,
            errors = listOf(WebClientResponseException(400, "Bad Request", null, null, null)),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        val response = gateway.getEvents(prisonId, nomisId, eventType, fromDateTime, limit, requestContext)

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
