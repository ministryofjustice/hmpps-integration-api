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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleAllocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleInstance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleSuspension
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesExclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPayRate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSuitabilityCriteria
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetActivitiesSuitabilityCriteriaService::class],
)
class GetActivitiesSuitabilityCriteriaServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val getScheduleDetailsService: GetScheduleDetailsService,
  val getActivitiesSuitabilityCriteriaService: GetActivitiesSuitabilityCriteriaService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val filters = RoleFilters(prisons = listOf(prisonId))
      val scheduleId = 123456L
      val activitySchedule =
        ActivitiesActivityScheduleDetailed(
          id = 123L,
          instances =
            listOf(
              ActivitiesActivityScheduleInstance(
                id = 12L,
                date = "2022-10-20",
                startTime = "09:00",
                endTime = "12:00",
                timeSlot = "AM",
                cancelled = false,
                attendances =
                  listOf(
                    ActivitiesAttendance(
                      id = 123L,
                      scheduledInstanceId = 12L,
                      prisonerNumber = "A1234AA",
                      status = "ACTIVE",
                      editable = true,
                      payable = false,
                      attendanceReason = null,
                      comment = null,
                      recordedTime = null,
                      recordedBy = null,
                      payAmount = null,
                      bonusAmount = null,
                      pieces = null,
                      issuePayment = null,
                      incentiveLevelWarningIssued = null,
                      otherAbsenceReason = null,
                      caseNoteText = null,
                      attendanceHistory = null,
                    ),
                  ),
                cancelledTime = null,
                cancelledBy = null,
              ),
            ),
          allocations =
            listOf(
              ActivitiesActivityScheduleAllocation(
                id = 56L,
                prisonerNumber = "A1234AA",
                bookingId = 78L,
                activitySummary = "Summary",
                activityId = 1L,
                scheduleId = 2L,
                scheduleDescription = "Description",
                isUnemployment = true,
                prisonPayBand =
                  ActivitiesPrisonPayBand(
                    id = 123456L,
                    displaySequence = 1,
                    alias = "Prison",
                    description = "pay band description",
                    nomisPayBand = 1,
                    prisonCode = prisonId,
                    createdTime = null,
                    createdBy = null,
                    updatedTime = null,
                    updatedBy = null,
                  ),
                startDate = "2022-10-20",
                endDate = null,
                allocatedTime = null,
                allocatedBy = null,
                deallocatedTime = null,
                deallocatedBy = null,
                deallocatedReason = null,
                suspendedTime = null,
                suspendedBy = null,
                suspendedReason = null,
                status = "ACTIVE",
                plannedDeallocation = null,
                plannedSuspension = null,
                exclusions =
                  listOf(
                    ActivitiesExclusion(
                      weekNumber = 1,
                      timeSlot = "AM",
                      monday = true,
                      tuesday = true,
                      wednesday = true,
                      thursday = true,
                      friday = true,
                      saturday = true,
                      sunday = true,
                      customStartTime = null,
                      customEndTime = null,
                      daysOfWeek = listOf(DayOfWeek.SUNDAY),
                    ),
                  ),
              ),
            ),
          description = "allocation description",
          suspensions =
            listOf(
              ActivitiesActivityScheduleSuspension(
                suspendedFrom = "2022-10-20",
                suspendedUntil = "2022-10-21",
              ),
            ),
          internalLocation =
            ActivitiesInternalLocation(
              id = 1,
              code = "code",
              description = "location description",
              dpsLocationId = null,
            ),
          capacity = 1,
          activity =
            ActivitiesActivity(
              id = 123L,
              prisonCode = prisonId,
              attendanceRequired = true,
              inCell = true,
              onWing = true,
              offWing = true,
              pieceWork = true,
              outsideWork = true,
              payPerSession = "pay",
              summary = "activity summary",
              description = "activity description",
              category =
                ActivitiesActivityCategory(
                  id = 1,
                  code = "code",
                  description = "activity description",
                  name = "activity name",
                ),
              riskLevel = "NORMAL",
              minimumEducationLevel =
                listOf(
                  ActivitiesMinimumEducationLevel(
                    id = 123L,
                    educationLevelCode = "code",
                    educationLevelDescription = "description",
                    studyAreaCode = "code",
                    studyAreaDescription = "description",
                  ),
                ),
              endDate = null,
              capacity = 10,
              allocated = 4,
              createdTime = LocalDateTime.now(),
              activityState = "state",
              paid = false,
            ),
          scheduleWeeks = 1,
          slots =
            listOf(
              ActivitiesSlot(
                id = 123L,
                timeSlot = "PM",
                weekNumber = 1,
                startTime = "09:00",
                endTime = "12:00",
                daysOfWeek = listOf("SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY"),
                mondayFlag = true,
                tuesdayFlag = true,
                wednesdayFlag = true,
                thursdayFlag = true,
                fridayFlag = true,
                saturdayFlag = true,
                sundayFlag = true,
              ),
            ),
          startDate = "2022-10-20",
          endDate = null,
          runsOnBankHoliday = true,
          updatedTime = null,
          updatedBy = null,
          usePrisonRegimeTime = true,
        )

      val suitabilityCriteria =
        ActivitiesSuitabilityCriteria(
          riskLevel = "medium",
          isPaid = true,
          payRates =
            listOf(
              ActivitiesPayRate(
                id = 123456L,
                incentiveNomisCode = "BAS",
                incentiveLevel = "Basic",
                prisonPayBand =
                  ActivitiesPrisonPayBand(
                    id = 123456,
                    displaySequence = 1,
                    alias = "Low",
                    description = "Pay band 1",
                    nomisPayBand = 1,
                    prisonCode = "MDI",
                    createdTime = "2025-06-11T09:05:01.364Z",
                    createdBy = "string",
                    updatedTime = "2025-06-11T09:05:01.364Z",
                    updatedBy = "string",
                  ),
                rate = 150,
                pieceRate = 150,
                pieceRateItems = 10,
                startDate = LocalDate.now(),
              ),
            ),
          minimumEducationLevel =
            listOf(
              ActivitiesMinimumEducationLevel(
                id = 123456L,
                educationLevelCode = "BASIC",
                educationLevelDescription = "Basic",
                studyAreaCode = "ENGLA",
                studyAreaDescription = "English Language",
              ),
            ),
        )

      beforeEach {
        Mockito.reset(getScheduleDetailsService, activitiesGateway)

        whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = activitySchedule.toActivityScheduleDetailed(), errors = emptyList()))
      }

      it("should return suitability criteria") {
        whenever(activitiesGateway.getActivitySuitabilityCriteria(scheduleId)).thenReturn(Response(data = suitabilityCriteria))

        val result = getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters)
        result.data.shouldBe(suitabilityCriteria.toSuitabilityCriteria())
        result.errors.shouldBeEmpty()
      }

      it("should return an error if filter check fails") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from consumer prison access check",
            ),
          )
        whenever(activitiesGateway.getActivitySuitabilityCriteria(scheduleId)).thenReturn(Response(data = suitabilityCriteria))
        whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters)
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
        whenever(activitiesGateway.getActivitySuitabilityCriteria(scheduleId)).thenReturn(Response(data = null, errors = errors))

        val result = getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
