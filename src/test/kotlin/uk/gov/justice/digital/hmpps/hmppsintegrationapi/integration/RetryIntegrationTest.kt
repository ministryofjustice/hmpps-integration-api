package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import java.io.File
import java.time.Duration

class RetryIntegrationTest : IntegrationTestBase() {
  @Value("\${services.activities.base-url}")
  lateinit var baseUrl: String

  @MockitoBean lateinit var featureFlagConfig: FeatureFlagConfig

  @MockitoSpyBean lateinit var activitiesGateway: ActivitiesGateway

  lateinit var client: WebClientWrapper
  lateinit var wrapper: WebClientWrapper

  @BeforeEach
  fun setup() {
    client =
      WebClientWrapper(
        baseUrl,
        connectTimeoutMillis = 500,
        responseTimeoutSeconds = 1,
        featureFlagConfig = featureFlagConfig,
      )
    wrapper = spy(client)
    whenever(wrapper.MIN_BACKOFF_DURATION).thenReturn(Duration.ofSeconds(0))
    ReflectionTestUtils.setField(activitiesGateway, "webClient", wrapper)

    activitiesMockServer.resetValidator()
  }

  @Nested
  @DisplayName("GET /v1/activities/{activityId}/schedules")
  inner class GetActivitySchedules {
    private val activityId = 123456L
    private val path = "/v1/activities/$activityId/schedules"

    @Test
    fun `returns schedules (requestList) on 3rd retry with feature flag enabled for http status 502`() {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 1",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )

      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("activities-schedule-response")))

      activitiesMockServer.assertValidationPassed()
    }

    @Test
    fun `returns failure (requestList) after 3rd retry with feature flag enabled for http status 502`() {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 2",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 502,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )

      val response = callApi(path).andReturn().response.contentAsJson<ErrorResponse>()
      response.status.shouldBe(500)
      response.userMessage shouldBe "External Service failed to process after max retries"
    }

    @Test
    fun `return schedules continues to fail without retry fails on first attempt with feature flag disabled`() {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 3",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )
      callApi(path)
        .andExpect(MockMvcResultMatchers.status().is5xxServerError)
    }
  }

  @Nested
  @DisplayName("GET /v1/activities/schedule/{scheduleId}")
  inner class GetScheduleDetails {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId"

    @Test
    fun `returns schedule details (request) on 3rd retry with feature flag enabled for http status 502`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 1",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      callApi(path)
        .andExpect(MockMvcResultMatchers.status().isOk)
        .andExpect(MockMvcResultMatchers.content().json(getExpectedResponse("activities-schedule-detailed-response")))

      activitiesMockServer.assertValidationPassed()
    }

    @Test
    fun `returns failure (request) after 3rd retry with feature flag enabled for http status 502`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 2",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 502,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      val response = callApi(path).andReturn().response.contentAsJson<ErrorResponse>()
      response.status.shouldBe(500)
      response.userMessage shouldBe "External Service failed to process after max retries"
    }

    @Test
    fun `return schedule continues to fail without retry fails on first attempt with feature flag disabled`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.RETRY_ALL_UPSTREAM_GETS)).thenReturn(false)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 3",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      callApi(path)
        .andExpect(MockMvcResultMatchers.status().is5xxServerError)
    }
  }
}
