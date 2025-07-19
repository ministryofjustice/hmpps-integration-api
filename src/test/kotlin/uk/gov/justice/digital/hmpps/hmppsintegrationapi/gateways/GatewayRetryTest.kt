package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertThrows
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ApplicationContextProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.ResponseException
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
@SpringBootTest(classes = [ApplicationContextProvider::class])
class GatewayRetryTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val scheduleId = 123456L
      val activityId = 123456L

      beforeEach {
        mockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
      }

      afterEach {
        mockServer.stop()
        mockServer.resetValidator()
      }

      it("Returns an activity schedule with feature flag disabled") {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
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

      it("Returns a bad request error with feature flag disabled") {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
        mockServer.stubForGet(
          "/integration-api/schedules/$scheduleId",
          "{}",
          HttpStatus.BAD_REQUEST,
        )
        val result = activitiesGateway.getActivityScheduleById(scheduleId)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }

      it("Returns activity schedules with feature flag disabled") {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
        mockServer.stubForGet(
          "/integration-api/activities/$activityId/schedules",
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
        )
        val result = activitiesGateway.getActivitySchedules(activityId)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        mockServer.assertValidationPassed()
      }

      listOf(502, 503, 504, 522, 599, 499, 408).forEach {
        it("Returns schedules on 3rd retry with feature flag enabled for http status $it") {
          whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
          mockServer.stubForRetry(
            scenario = "Retry Success",
            numberOfRequests = 4,
            failedStatus = it,
            endStatus = 200,
            path = "/integration-api/activities/$activityId/schedules",
            body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
          )
          val result = activitiesGateway.getActivitySchedules(activityId)
          result.errors.shouldBeEmpty()
          result.data.shouldNotBeNull()
        }
      }

      it("Returns error after 3rd retry with feature flag enabled for http status 502") {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
        mockServer.stubForRetry(
          scenario = "Retry Failed",
          numberOfRequests = 4,
          failedStatus = 502,
          endStatus = 502,
          path = "/integration-api/activities/$activityId/schedules",
          body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
        )
        val exception =
          assertThrows(ResponseException::class.java) {
            activitiesGateway.getActivitySchedules(activityId)
          }
        exception.message.shouldBe("External Service failed to process after max retries")
      }
    },
  )
