package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.san

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiClient
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RestApiResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewScheduleStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewSchedules
import java.util.UUID

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
)
class GetReviewSchedulesForPrisonerTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
) : DescribeSpec(
    {
      val prisonerNumber = "G4887VE"
      val path = "/profile/$prisonerNumber/reviews/review-schedules?includeAllHistory=true"
      val requestContext = buildRequestContext("testUser")
      val authToken = "ABC123"
      val headers = mapOf("Authorization" to "Bearer $authToken")

      fun responseJson() =
        """
         {
          "reviewSchedules": [
              {
                  "reference": "39ee07c2-1607-42af-a2e8-af6215505ad9",
                  "deadlineDate": "2025-07-24",
                  "status": "SCHEDULED",
                  "createdBy": "SMCALLISTER_GEN",
                  "createdByDisplayName": "Stephen Mcallister",
                  "createdAt": "2025-07-21T14:08:47.575496Z",
                  "createdAtPrison": "MDI",
                  "updatedBy": "SMCALLISTER_GEN",
                  "updatedByDisplayName": "Stephen Mcallister",
                  "updatedAt": "2025-07-21T14:08:47.57551Z",
                  "updatedAtPrison": "MDI",
                  "reviewCompletedDate": null,
                  "reviewKeyedInBy": null,
                  "reviewCompletedBy": null,
                  "reviewCompletedByJobRole": null,
                  "exemptionReason": null,
                  "version": 1
              }
          ]
      }
          """.removeWhitespaceAndNewlines()

      beforeEach {
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("SAN", requestContext)).thenReturn(authToken)
      }

      // Note that HMPPS Auth token use is verified by the primary unit tests now

      it("returns review schedules") {
        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(path), eq(PlanReviewSchedules::class), eq(headers), isNull()))
          .thenReturn(RestApiResponse("?", HttpStatus.OK, RestApiClient.mapResponse(responseJson(), PlanReviewSchedules::class)))

        val gateway = SANGateway("http://localhost", apiClient, hmppsAuthGateway)

        val response = gateway.getReviewSchedules(prisonerNumber, requestContext)

        response.data.shouldNotBeNull()
        val schedules = response.data.planReviewSchedules
        schedules.size.shouldBe(1)

        val schedule = schedules.first()
        schedule.reference.shouldBe(UUID.fromString("39ee07c2-1607-42af-a2e8-af6215505ad9"))
        schedule.deadlineDate!!.toString().shouldBe("2025-07-24")
        schedule.status.shouldBe(PlanReviewScheduleStatus.SCHEDULED)
        schedule.createdBy.shouldBe("SMCALLISTER_GEN")
        schedule.createdByDisplayName.shouldBe("Stephen Mcallister")
        schedule.createdAt.toString().shouldBe("2025-07-21T14:08:47.575496Z")
        schedule.createdAtPrison.shouldBe("MDI")
        schedule.updatedBy.shouldBe("SMCALLISTER_GEN")
        schedule.updatedByDisplayName.shouldBe("Stephen Mcallister")
        schedule.updatedAt.toString().shouldBe("2025-07-21T14:08:47.575510Z")
        schedule.updatedAtPrison.shouldBe("MDI")
        schedule.reviewCompletedDate.shouldBe(null)
        schedule.reviewKeyedInBy.shouldBe(null)
        schedule.reviewCompletedBy.shouldBe(null)
        schedule.reviewCompletedByJobRole.shouldBe(null)
        schedule.exemptionReason.shouldBe(null)
        schedule.version.shouldBe(1)
      }

      it("can can handle errors responses") {
        val apiClient: RestApiClient = mock()
        whenever(apiClient.get(eq(path), eq(PlanReviewSchedules::class), eq(headers), isNull())).thenReturn(
          RestApiResponse(
            "Test",
            HttpStatus.NOT_FOUND,
            null,
            listOf(WebClientResponseException(404, "PlanReviewSchedules not found", null, null, null)),
          ),
        )
        val gateway = SANGateway("http://localhost", apiClient, hmppsAuthGateway)

        val response = gateway.getReviewSchedules(prisonerNumber, requestContext)

        response shouldNotBe null
        response.errors.size shouldBe 1
        response.errors[0].description shouldBe "404 PlanReviewSchedules not found"
      }
    },
  )
