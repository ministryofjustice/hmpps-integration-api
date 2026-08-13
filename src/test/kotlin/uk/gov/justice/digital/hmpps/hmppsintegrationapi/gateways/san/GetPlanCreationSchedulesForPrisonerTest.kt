package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.san

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.RESTAPICLIENT_FOR_SAN_GATEWAY
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [SANGateway::class],
)
class GetPlanCreationSchedulesForPrisonerTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val sanGateway: SANGateway,
) : DescribeSpec(
    {
      val apiMockServer = ApiMockServer.create(UpstreamApi.SAN)
      val prisonerNumber = "G4887VE"
      val path = "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true"
      val requestContext = buildRequestContext("testUser")

      fun responseJson() =
        """
          {
    "planCreationSchedules": [
        {
            "reference": "44052fd9-bf6c-41bc-8308-6839a7048836",
            "status": "SCHEDULED",
            "createdBy": "system",
            "createdByDisplayName": "system",
            "createdAt": "2025-07-16T08:48:11.844724Z",
            "createdAtPrison": "BXI",
            "updatedBy": "system",
            "updatedByDisplayName": "system",
            "updatedAt": "2025-07-16T08:48:11.844737Z",
            "updatedAtPrison": "BXI",
            "deadlineDate": "2025-10-06",
            "exemptionReason": null,
            "exemptionDetail": null,
            "needSources": [
                "ALN_SCREENER",
                "LDD_SCREENER"
            ],
            "version": 1
        },
        {
            "reference": "44052fd9-bf6c-41bc-8308-6839a7048836",
            "status": "EXEMPT_PRISONER_NOT_COMPLY",
            "createdBy": "system",
            "createdByDisplayName": "system",
            "createdAt": "2025-07-16T08:48:11.844724Z",
            "createdAtPrison": "BXI",
            "updatedBy": "SMCALLISTER_GEN",
            "updatedByDisplayName": "Stephen Mcallister",
            "updatedAt": "2025-07-16T08:48:29.143136Z",
            "updatedAtPrison": "MDI",
            "deadlineDate": null,
            "exemptionReason": "EXEMPT_REFUSED_TO_ENGAGE",
            "exemptionDetail": "aa",
            "needSources": [
                "ALN_SCREENER",
                "LDD_SCREENER"
            ],
            "version": 2
        }
    ]
}
          """.removeWhitespaceAndNewlines()

      val responseData = defaultObjectMapper.readValue(responseJson(), PlanCreationSchedules::class.java)

      beforeEach {
        apiMockServer.start()
        apiMockServer.stubForGet(
          path,
          responseJson(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("SAN", requestContext)).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        apiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        sanGateway.getPlanCreationSchedules(prisonerNumber, requestContext)

        verify(hmppsAuthGateway, times(1)).getClientToken("SAN", requestContext)
      }

      it("returns plan creation schedules for the matching person ID") {
        val response = sanGateway.getPlanCreationSchedules(prisonerNumber, requestContext)
        response.data.shouldNotBeNull()
        response.data.planCreationSchedules.size
          .shouldBe(2)
      }

      it("can use the RestApiClient") {
        // Given
        val authToken = "ABC123"
        val headers = mapOf("Authorization" to "Bearer $authToken")

        val features = FeatureFlagConfig(mapOf(RESTAPICLIENT_FOR_SAN_GATEWAY to true))
        val flagRequestContext = buildRequestContext("testUser", featureFlags = features)

        val authGateway: HmppsAuthGateway = mock()
        whenever(authGateway.getClientToken("SAN", flagRequestContext)).thenReturn(authToken)

        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(path), eq(PlanCreationSchedules::class), eq(headers), isNull()))
          .thenReturn(RestApiResponse("?", HttpStatus.OK, responseData))

        val gateway = SANGateway("http://localhost", apiClient)
        gateway.hmppsAuthGateway = authGateway

        // When
        val response = gateway.getPlanCreationSchedules(prisonerNumber, flagRequestContext)

        // Then
        response.errors.size shouldBe 0
        response.data.planCreationSchedules.size shouldBe 2
        response.data.planCreationSchedules[0].status shouldBe PlanCreationStatus.SCHEDULED
        response.data.planCreationSchedules[0]
          .reference
          .toString() shouldBe "44052fd9-bf6c-41bc-8308-6839a7048836"
      }
    },
  )
