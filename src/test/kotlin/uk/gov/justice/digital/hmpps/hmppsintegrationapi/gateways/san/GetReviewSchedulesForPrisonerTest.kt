package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonerAlerts

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.SANGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PlanReviewScheduleStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.util.UUID

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [SANGateway::class],
)
class GetReviewSchedulesForPrisonerTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val sanGateway: SANGateway,
) : DescribeSpec(
    {
      val apiMockServer = ApiMockServer.create(UpstreamApi.SAN)
      val prisonerNumber = "G4887VE"
      val path = "/profile/$prisonerNumber/reviews/review-schedules?includeAllHistory=true"

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
        apiMockServer.start()
        apiMockServer.stubForGet(
          path,
          responseJson(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("SAN")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        apiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        sanGateway.getPlanCreationSchedules(prisonerNumber)

        verify(hmppsAuthGateway, times(1)).getClientToken("SAN")
      }

      it("returns review schedules") {
        val response = sanGateway.getReviewSchedules(prisonerNumber)

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
    },
  )
