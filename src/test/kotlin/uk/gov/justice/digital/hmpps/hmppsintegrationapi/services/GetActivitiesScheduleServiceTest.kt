package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetActivitiesScheduleService::class],
)
class GetActivitiesScheduleServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getActivitiesScheduleService: GetActivitiesScheduleService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val filters = ConsumerFilters(prisons = listOf(prisonId))
      val activityId = 123456L
      val activitiesActivitySchedule =
        listOf(
          ActivitiesActivitySchedule(
            id = 1001L,
            description = "Morning Education Class",
            internalLocation =
              ActivitiesInternalLocation(
                id = 201,
                code = "EDU-ROOM1",
                description = "Education Room 1",
                dpsLocationId = "MDI-EDU-ROOM1",
              ),
            capacity = 25,
            activity =
              ActivitiesActivity(
                id = 2001L,
                prisonCode = "MDI",
                attendanceRequired = true,
                inCell = false,
                onWing = false,
                offWing = true,
                pieceWork = false,
                outsideWork = true,
                payPerSession = "F",
                summary = "Gardening Project",
                description = "Maintain the prison gardens and grow vegetables.",
                category =
                  ActivitiesActivityCategory(
                    id = 4L,
                    code = "GAR",
                    name = "Gardening",
                    description = "Horticultural activities",
                  ),
                riskLevel = "LOW",
                minimumEducationLevel =
                  listOf(
                    ActivitiesMinimumEducationLevel(
                      id = 10L,
                      educationLevelCode = "ENTRY",
                      educationLevelDescription = "Entry Level",
                      studyAreaCode = "HORT",
                      studyAreaDescription = "Horticulture",
                    ),
                  ),
                endDate = "2024-12-31",
                capacity = 15,
                allocated = 12,
                createdTime = LocalDateTime.now(),
                activityState = "LIVE",
                paid = true,
              ),
            scheduleWeeks = 2,
            slots =
              listOf(
                ActivitiesSlot(
                  id = 101L,
                  timeSlot = "AM",
                  weekNumber = 1,
                  startTime = "09:00",
                  endTime = "12:00",
                  daysOfWeek = "Mon,Wed,Fri",
                  mondayFlag = true,
                  tuesdayFlag = false,
                  wednesdayFlag = true,
                  thursdayFlag = false,
                  fridayFlag = true,
                  saturdayFlag = false,
                  sundayFlag = false,
                ),
              ),
            startDate = "2024-01-15",
            endDate = "2024-07-15",
            usePrisonRegimeTime = true,
          ),
        )

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("should return an activity schedule") {
        whenever(activitiesGateway.getActivitySchedules(activityId)).thenReturn(Response(data = activitiesActivitySchedule))

        val result = getActivitiesScheduleService.execute(activityId, filters)
        result.data.shouldBe(activitiesActivitySchedule.map { it.toActivitySchedule() })
        result.errors.shouldBeEmpty()
      }

      it("should return an error if consumer filter check fails") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from consumer prison access check",
            ),
          )
        whenever(activitiesGateway.getActivitySchedules(activityId)).thenReturn(Response(data = activitiesActivitySchedule))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = errors))

        val result = getActivitiesScheduleService.execute(activityId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }

      it("should return an error if gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from gateway",
            ),
          )
        whenever(activitiesGateway.getActivitySchedules(activityId)).thenReturn(Response(data = null, errors = errors))

        val result = getActivitiesScheduleService.execute(activityId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
