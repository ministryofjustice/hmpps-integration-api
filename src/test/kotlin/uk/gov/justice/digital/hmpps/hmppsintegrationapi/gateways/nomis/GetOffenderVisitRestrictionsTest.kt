package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi.NomisOffenderVisitRestrictions

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetOffenderVisitRestrictionsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val restApiClient: RestApiClient,
  val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "zyx987"
      val offenderRestrictionsPath = "/api/offenders/$offenderNo/offender-restrictions"
      val responseJson =
        """
            {
              "bookingId": 9007199254740991,
              "offenderRestrictions": [
                {
                  "restrictionId": 9007199254740991,
                  "comment": "string",
                  "restrictionType": "string",
                  "restrictionTypeDescription": "string",
                  "startDate": "1980-01-01",
                  "expiryDate": "1980-01-01",
                  "active": true
                }
              ]
            }
        """.removeWhitespaceAndNewlines()

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          offenderRestrictionsPath,
          responseJson,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        nomisApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonApiGateway.getOffenderVisitRestrictions(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns offender visit restrictions for the matching person ID") {
        val response = prisonApiGateway.getOffenderVisitRestrictions(offenderNo)

        response.data.shouldNotBeNull()
        response.data!!.count().shouldBeGreaterThan(0)
      }

      it("returns a person with an empty list of restrictions when no restrictions are found") {
        nomisApiMockServer.stubForGet(
          offenderRestrictionsPath,
          """
            {
              "bookingId": 9007199254740991,
              "offenderRestrictions": []
            }
            """.removeWhitespaceAndNewlines(),
        )

        val response = prisonApiGateway.getOffenderVisitRestrictions(offenderNo)

        response.data.shouldBeEmpty()
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubForGet(offenderRestrictionsPath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getOffenderVisitRestrictions(offenderNo)

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

      it("returns an error when 400 Bad Request is returned because of an invalid request") {
        nomisApiMockServer.stubForGet(offenderRestrictionsPath, "", HttpStatus.BAD_REQUEST)

        val response = prisonApiGateway.getOffenderVisitRestrictions(offenderNo)

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

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_PRISON_API_GATEWAY to true))

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("NOMIS", null)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(offenderRestrictionsPath), eq(NomisOffenderVisitRestrictions::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.OK,
            RestApiClient.mapResponse(responseJson, NomisOffenderVisitRestrictions::class),
          ),
        )

        val gateway = PrisonApiGateway("http://localhost", features, apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getOffenderVisitRestrictions(offenderNo)

        // Then
        response.data.shouldNotBeNull()
        response.data!!.count().shouldBeGreaterThan(0)
      }
    },
  )
