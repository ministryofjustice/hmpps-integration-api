package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiPrisonTimeline
import java.io.File
import kotlin.collections.count

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetPrisonTimelineForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "A7748DZ"
      val prisonTimelinePath = "/api/offenders/$offenderNo/prison-timeline"
      val requestContext = buildRequestContext("testUser")
      val responseJson =
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/GetPrisonTimelineForPersonResponse.json",
        ).readText()
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          prisonTimelinePath,
          responseJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS", requestContext)).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getPrisonTimelineForPerson(offenderNo, requestContext)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS", requestContext)
      }

      it("returns latest sentence key dates for a person with the matching ID") {
        val response = prisonApiGateway.getPrisonTimelineForPerson(offenderNo, requestContext)

        response.data
          ?.prisonPeriod
          ?.count()
          ?.shouldBeGreaterThan(0)
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(prisonTimelinePath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getPrisonTimelineForPerson(offenderNo, requestContext)

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
        val requestContext = buildRequestContext("testUser", featureFlags = features)

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", requestContext)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(prisonTimelinePath), eq(PrisonApiPrisonTimeline::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, PrisonApiPrisonTimeline::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getPrisonTimelineForPerson(offenderNo, requestContext)

        // Then
        response.data
          ?.prisonPeriod
          ?.count()
          ?.shouldBeGreaterThan(0)
      }
    },
  )
