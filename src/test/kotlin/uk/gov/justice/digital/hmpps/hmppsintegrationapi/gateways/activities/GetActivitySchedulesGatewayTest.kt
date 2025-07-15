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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetActivitySchedulesGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val activityId = 123456L

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
        activitiesGateway.getActivitySchedules(activityId)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns an activity schedule") {
        mockServer.stubForGet(
          "/integration-api/activities/$activityId/schedules",
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesSchedule.json").readText(),
        )

        val result = activitiesGateway.getActivitySchedules(activityId)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data[0].shouldBe(
          ActivitiesActivitySchedule(
            id = activityId,
            description = "Monday AM Houseblock 3",
            internalLocation =
              ActivitiesInternalLocation(
                id = 98877667,
                code = "EDU-ROOM-1",
                description = "Education - R1",
                dpsLocationId = "b7602cc8-e769-4cbb-8194-62d8e655992a",
              ),
            capacity = 10,
            activity =
              ActivitiesActivity(
                id = 123456,
                prisonCode = "MDI",
                attendanceRequired = false,
                inCell = false,
                onWing = false,
                offWing = false,
                pieceWork = false,
                outsideWork = false,
                payPerSession = "H",
                summary = "Maths level 1",
                description = "A basic maths course suitable for introduction to the subject",
                category =
                  ActivitiesActivityCategory(
                    id = 1,
                    code = "LEISURE_SOCIAL",
                    name = "Leisure and social",
                    description = "Such as association, library time and social clubs, like music or art",
                  ),
                riskLevel = "high",
                minimumEducationLevel =
                  listOf(
                    ActivitiesMinimumEducationLevel(
                      id = 123456,
                      educationLevelCode = "Basic",
                      educationLevelDescription = "Basic",
                      studyAreaCode = "ENGLA",
                      studyAreaDescription = "English Language",
                    ),
                  ),
                endDate = "2022-12-21",
                capacity = 0,
                allocated = 0,
                createdTime = LocalDateTime.parse("2022-09-01T09:01:02"),
                activityState = "live",
                paid = true,
              ),
            scheduleWeeks = 1,
            slots =
              listOf(
                ActivitiesSlot(
                  id = 123456,
                  timeSlot = "AM",
                  weekNumber = 1,
                  startTime = "9:00",
                  endTime = "11:30",
                  daysOfWeek = listOf("Mon", "Tue", "Wed"),
                  mondayFlag = true,
                  tuesdayFlag = true,
                  wednesdayFlag = true,
                  thursdayFlag = false,
                  fridayFlag = false,
                  saturdayFlag = false,
                  sundayFlag = false,
                ),
              ),
            startDate = "2022-09-21",
            endDate = "2022-10-21",
            usePrisonRegimeTime = true,
          ),
        )
      }

      it("Returns a bad request error") {
        mockServer.stubForGet(
          "/integration-api/activities/$activityId/schedules",
          "{}",
          HttpStatus.BAD_REQUEST,
        )

        val result = activitiesGateway.getActivitySchedules(activityId)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
