package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiMovements
import java.io.File
import kotlin.collections.count

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetMovementsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "A7748DZ"
      val prisonTimelinePath = "/api/movements/offender/$offenderNo?movementTypes=TRN&movementTypes=CRT&allBookings=true"
      val requestContext = buildRequestContext("testUser")
      val responseJson =
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/GetMovementsForPersonResponse.json",
        ).readText()
      val apiClient: RestApiClient = mock()
      val gateway = PrisonApiGateway("http://localhost", featureFlagConfig, apiClient)

      beforeEach {
        nomisApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        Mockito.reset(apiClient)
        whenever(hmppsAuthGateway.getClientToken("NOMIS", requestContext)).thenReturn(HmppsAuthMockServer.TOKEN)
        whenever(apiClient.get(eq(prisonTimelinePath), eq(PrisonApiMovements::class), any(), any()))
          .thenReturn(
            RestApiResponse(
              "Test",
              HttpStatus.OK,
              RestApiClient.mapResponse(
                responseJson,
                PrisonApiMovements::class,
              ),
            ),
          )
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        gateway.getMovementsForPerson(offenderNo, requestContext)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS", requestContext)
      }

      it("returns movements for a person with the matching ID") {
        val response = gateway.getMovementsForPerson(offenderNo, requestContext)

        response.data
          ?.movements
          ?.count()
          ?.shouldBeGreaterThan(0)
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(prisonTimelinePath, "", HttpStatus.NOT_FOUND)

        val response = gateway.getMovementsForPerson(offenderNo, requestContext)

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
    },
  )
