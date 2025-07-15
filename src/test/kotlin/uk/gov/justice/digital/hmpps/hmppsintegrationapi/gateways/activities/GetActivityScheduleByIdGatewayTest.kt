package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetActivityScheduleByIdGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val scheduleId = 123456L

      beforeEach {
        mockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterEach {
        mockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getActivityScheduleById(scheduleId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns an activity schedule") {
        mockServer.stubForGet(
          "/integration-api/schedules/$scheduleId",
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivityScheduleById.json").readText(),
        )

        val result = activitiesGateway.getActivityScheduleById(scheduleId)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data.id.shouldBe(123456)
        result.data.instances.size
          .shouldBe(1)
        result.data.allocations.size
          .shouldBe(1)
        result.data.suspensions.size
          .shouldBe(1)
        result.data.internalLocation.shouldNotBeNull()
        result.data.internalLocation.id
          .shouldBe(98877667)
        result.data.activity.id
          .shouldBe(123456)
        result.data.slots.size
          .shouldBe(1)
      }

      it("Returns a bad request error") {
        mockServer.stubForGet(
          "/integration-api/schedules/$scheduleId",
          "{}",
          HttpStatus.BAD_REQUEST,
        )

        val result = activitiesGateway.getActivityScheduleById(scheduleId)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
