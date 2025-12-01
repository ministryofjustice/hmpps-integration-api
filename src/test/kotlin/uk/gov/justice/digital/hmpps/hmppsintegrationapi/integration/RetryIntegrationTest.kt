package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import java.io.File
import java.time.Duration
import kotlin.collections.first

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
        initialBackOffDuration = Duration.ofSeconds(0),
      )
    wrapper = spy(client)
    ReflectionTestUtils.setField(activitiesGateway, "webClient", wrapper)

    activitiesMockServer.resetValidator()
  }

  @Nested
  @DisplayName("GET /v1/activities/{activityId}/schedules")
  inner class GetActivitySchedules {
    private val activityId = 123456L
    private val path = "/v1/activities/$activityId/schedules"

    @Test
    fun `returns schedules (requestList) on 3rd retry for http status 502`() {
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 1",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )

      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("activities-schedule-response")))

      activitiesMockServer.assertValidationPassed()
    }

    @Test
    fun `returns failure (requestList) after 3rd retry for http status 502`() {
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
      response.userMessage shouldBe "External Service failed to process after 3 retries with Call to upstream api ACTIVITIES failed. GET for /integration-api/activities/123456/schedules returned 502"
    }

    @Test
    fun `returns failure (requestList) when upstream service not available after 3 retries`() {
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 3",
        numberOfRequests = 4,
        failedStatus = -1,
        endStatus = 502,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )

      val response = callApi(path).andReturn().response.contentAsJson<ErrorResponse>()
      response.status.shouldBe(500)
      response.userMessage shouldBe "External Service failed to process after 3 retries with Call to upstream api ACTIVITIES failed. GET for /integration-api/activities/123456/schedules returned 502"
    }

    @Test
    fun `returns success (requestList) when upstream service not available for 3 times and succeeds on last retry`() {
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for requestList 4",
        numberOfRequests = 4,
        failedStatus = -1,
        endStatus = 200,
        path = "/integration-api/activities/$activityId/schedules",
        body = File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
      )
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("activities-schedule-response")))
    }
  }

  @Nested
  @DisplayName("GET /v1/activities/schedule/{scheduleId}")
  inner class GetScheduleDetails {
    val scheduleId = 123456L
    val path = "/v1/activities/schedule/$scheduleId"

    @Test
    fun `returns schedule details (request) on 3rd retry for http status 502`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 1",
        numberOfRequests = 4,
        failedStatus = 502,
        endStatus = 200,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("activities-schedule-detailed-response")))

      activitiesMockServer.assertValidationPassed()
    }

    @Test
    fun `returns failure (request) after 3rd retry for http status 502`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
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
      response.userMessage shouldBe "External Service failed to process after 3 retries with Call to upstream api ACTIVITIES failed. GET for /integration-api/schedules/123456 returned 502"
    }

    @Test
    fun `returns failure (request) when upstream service not available after 3 retries`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 3",
        numberOfRequests = 4,
        failedStatus = -1,
        endStatus = 502,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      val response = callApi(path).andReturn().response.contentAsJson<ErrorResponse>()
      response.status.shouldBe(500)
      response.userMessage shouldBe "External Service failed to process after 3 retries with Call to upstream api ACTIVITIES failed. GET for /integration-api/schedules/123456 returned 502"
    }

    @Test
    fun `returns success (requestList) when upstream service not available for 3 times and succeeds on last retry`() {
      whenever(featureFlagConfig.getConfigFlagValue(FeatureFlagConfig.USE_SCHEDULE_DETAIL_ENDPOINT)).thenReturn(true)
      activitiesMockServer.stubForRetry(
        scenario = "Retry scenario for request 4",
        numberOfRequests = 4,
        failedStatus = -1,
        endStatus = 200,
        path = "/integration-api/schedules/$scheduleId",
        body = File("$gatewaysFolder/activities/fixtures/GetActivityScheduleById.json").readText(),
      )

      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("activities-schedule-detailed-response")))
    }
  }

  @Nested
  @DisplayName("Delius POST Retries")
  @TestPropertySource(properties = ["services.ndelius.base-url=http://localhost:4201"])
  inner class DeliusPosts {
    @MockitoSpyBean lateinit var deliusGateway: NDeliusGateway

    @Value("\${services.ndelius.base-url}")
    lateinit var deliusBaseUrl: String

    lateinit var deliusClient: WebClientWrapper
    lateinit var deliusWrapper: WebClientWrapper

    val offenderSearchPath = "/search/probation-cases"
    val caseAccessPath = "/probation-cases/access"

    @BeforeEach
    internal fun setUp() {
      nDeliusMockServer.start()
      deliusClient =
        WebClientWrapper(
          deliusBaseUrl,
          connectTimeoutMillis = 500,
          initialBackOffDuration = Duration.ofSeconds(0),
        )
      deliusWrapper = spy(deliusClient)
      ReflectionTestUtils.setField(deliusGateway, "webClient", deliusWrapper)
    }

    @AfterEach
    internal fun tearDown() {
      nDeliusMockServer.stop()
    }

    @Test
    fun `ndelius getPersons succeeds after 2nd retry`() {
      nDeliusMockServer.stubForPostRetry(
        "Delius getPersons",
        offenderSearchPath,
        3,
        -1,
        200,
        """
        {
          "firstName": "Ahsoka",
          "surname": "Tano",
          "includeAliases": false
        }
        """.removeWhitespaceAndNewlines(),
        """
        [
          {
            "firstName": "Ahsoka",
            "surname": "Tano"
          }
        ]
        """.trimIndent(),
      )
      val response = deliusGateway.getPersons("Ahsoka", "Tano", null, null)
      response.data.count().shouldBe(1)
      response.data
        .first()
        .firstName
        .shouldBe("Ahsoka")
      response.data
        .first()
        .lastName
        .shouldBe("Tano")
    }

    @Test
    fun `ndelius getCaseAccess succeeds after 2nd retry`() {
      nDeliusMockServer.stubForPostRetry(
        "Delius getCaseAccess",
        caseAccessPath,
        3,
        -1,
        200,
        """
        {
          "crns": ["AB123456"]
        }
        """.removeWhitespaceAndNewlines(),
        """
        {
          "access": [{
            "crn": "AB123456",
            "userExcluded": false,
            "userRestricted": false
          }]
        }
        """.trimIndent(),
      )
      val response = deliusGateway.getCaseAccess("AB123456")
      response.data.shouldBe(CaseAccess("AB123456", false, false))
    }

    @Test
    fun `ndelius getOffender succeeds after 2nd retry`() {
      nDeliusMockServer.stubForPostRetry(
        "Delius getOffender",
        offenderSearchPath,
        3,
        -1,
        200,
        """
        {
          "crn": "AB123456"
        }
        """.removeWhitespaceAndNewlines(),
        """
        [
          {
            "firstName": "Test",
            "surname": "User"
          }
        ]
        """.trimIndent(),
      )
      val response = deliusGateway.getOffender("AB123456")
      response.data?.firstName.shouldBe("Test")
      response.data?.surname.shouldBe("User")
    }
  }
}
