package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReasonableAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.PrisonApiReferenceCode
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetReasonableAdjustmentTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  private val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val bookingId = "mockBooking"
      val domainPath = "/api/reference-domains/domains/HEALTH_TREAT/codes"
      var reasonableAdjustmentPath = "/api/bookings/$bookingId/reasonable-adjustments?type=a&type=b&type=c"
      val responseDomainJson = """
          [
            {"domain":"abc", "code":"a"},
            {"domain":"abc", "code":"b"},
            {"domain":"abc", "code":"c"}
          ]
        """

      val responseAdjustmentJson = """
            { "reasonableAdjustments":[
                {
                      "treatmentCode": "WHEELCHR_ACC",
                      "commentText": "abcd",
                      "startDate": "2010-06-21",
                      "endDate": "2010-06-21",
                      "treatmentDescription": "Wheelchair accessibility"
                 }
              ]
           }
        """
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          domainPath,
          responseDomainJson,
        )

        nomisApiMockServer.stubForGet(
          reasonableAdjustmentPath,
          responseAdjustmentJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getReasonableAdjustments(bookingId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(2)).getClientToken("NOMIS")
      }

      it("returns reasonable adjustment for a person with the matching ID") {
        val response = prisonApiGateway.getReasonableAdjustments(bookingId)

        response.data.count().shouldBe(1)
        response.data
          .first()
          .treatmentCode
          .shouldBe("WHEELCHR_ACC")
        response.data
          .first()
          .commentText
          .shouldBe("abcd")
        response.data
          .first()
          .startDate
          .shouldBe(LocalDate.parse("2010-06-21"))
        response.data
          .first()
          .endDate
          .shouldBe(LocalDate.parse("2010-06-21"))
        response.data
          .first()
          .treatmentDescription
          .shouldBe("Wheelchair accessibility")
      }

      it("returns an empty list when no reasonable adjustment are found") {
        nomisApiMockServer.stubForGet(domainPath, "[]")

        val response = prisonApiGateway.getReasonableAdjustments(bookingId)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 NOT FOUND is returned") {
        nomisApiMockServer.stubForGet(
          domainPath,
          """
        {
          "developerMessage": "cannot find person"
        }
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = prisonApiGateway.getReasonableAdjustments(bookingId)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.getList(eq(domainPath), eq(PrisonApiReferenceCode::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapListResponse(responseDomainJson, PrisonApiReferenceCode::class),
          ),
        )
        whenever(apiClient.get(eq(reasonableAdjustmentPath), eq(PrisonApiReasonableAdjustments::class), any(), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseAdjustmentJson, PrisonApiReasonableAdjustments::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getReasonableAdjustments(bookingId)

        // Then
        response.data.count().shouldBe(1)
        response.data
          .first()
          .treatmentCode
          .shouldBe("WHEELCHR_ACC")
        response.data
          .first()
          .commentText
          .shouldBe("abcd")
        response.data
          .first()
          .startDate
          .shouldBe(LocalDate.parse("2010-06-21"))
        response.data
          .first()
          .endDate
          .shouldBe(LocalDate.parse("2010-06-21"))
        response.data
          .first()
          .treatmentDescription
          .shouldBe("Wheelchair accessibility")
      }
    },
  )
