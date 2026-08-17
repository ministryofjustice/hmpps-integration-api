package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.san

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanCreationStatus

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
)
class GetPlanCreationSchedulesForPrisonerTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
) : DescribeSpec(
    {
      val prisonerNumber = "G4887VE"
      val path = "/profile/$prisonerNumber/plan-creation-schedule?includeAllHistory=true"
      val requestContext = buildRequestContext("testUser")
      val authToken = "ABC123"
      val headers = mapOf("Authorization" to "Bearer $authToken")

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

      beforeEach {
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("SAN", requestContext)).thenReturn(authToken)
      }

      // Note that HMPPS Auth token use is verified by the primary unit tests now

      it("returns plan creation schedules for the matching person ID") {
        // Given
        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(path), eq(PlanCreationSchedules::class), eq(headers), isNull()))
          .thenReturn(RestApiResponse("?", HttpStatus.OK, RestApiClient.mapResponse(responseJson(), PlanCreationSchedules::class)))

        val gateway = SANGateway("http://localhost", apiClient, hmppsAuthGateway)

        // When
        val response = gateway.getPlanCreationSchedules(prisonerNumber, requestContext)

        // Then
        response.errors.size shouldBe 0
        response.data.shouldNotBeNull()
        response.data.planCreationSchedules.size shouldBe 2
        response.data.planCreationSchedules[0].status shouldBe PlanCreationStatus.SCHEDULED
        response.data.planCreationSchedules[0]
          .reference
          .toString() shouldBe "44052fd9-bf6c-41bc-8308-6839a7048836"
      }
    },
  )
