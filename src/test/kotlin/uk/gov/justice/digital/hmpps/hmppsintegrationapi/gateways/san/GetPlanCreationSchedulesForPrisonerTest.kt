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

      it("returns plan creation schedules for the matching person ID") {
        val response = sanGateway.getPlanCreationSchedules(prisonerNumber)
        response.data.shouldNotBeNull()
        response.data.planCreationSchedules.size
          .shouldBe(2)
      }
    },
  )
