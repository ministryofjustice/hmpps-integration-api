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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstancesForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAdvanceAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetScheduledInstancesForPrisonerService::class],
)
class GetScheduledInstancesForPrisonerServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getScheduledInstancesForPrisonerService: GetScheduledInstancesForPrisonerService,
) : DescribeSpec(
    {
      val prisonCode = "MKI"
      val filters = ConsumerFilters(prisons = listOf(prisonCode))
      val prisonerId = "A1234AA"
      val activitiesActivityScheduledInstanceForPerson =
        listOf(
          ActivitiesActivityScheduledInstancesForPrisoner(
            id = 123456L,
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
            cancelledReason = null,
            cancelledIssuePayment = null,
            comment = null,
            previousScheduledInstanceId = null,
            previousScheduledInstanceDate = null,
            nextScheduledInstanceId = null,
            nextScheduledInstanceDate = null,
            advanceAttendances =
              listOf(
                ActivitiesAdvanceAttendance(
                  id = 123L,
                  scheduleInstanceId = 12L,
                  prisonerNumber = "A1234AA",
                  issuePayment = null,
                  payAmount = null,
                  recordedTime = null,
                  recordedBy = null,
                  attendanceHistory = null,
                ),
              ),
            activitySchedule =
              ActivitiesActivitySchedule(
                id = 13L,
                description = "Monday AM Houseblock 3",
                internalLocation = null,
                capacity = 10,
                activity =
                  ActivitiesActivity(
                    id = 123456,
                    prisonCode = "PVI",
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
                scheduleWeeks = 2,
                slots =
                  listOf(
                    ActivitiesSlot(
                      id = 101L,
                      timeSlot = "AM",
                      weekNumber = 1,
                      startTime = "09:00",
                      endTime = "12:00",
                      daysOfWeek = listOf("Mon", "Wed", "Fri"),
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
          ),
        )

      val activityScheduledInstanceForPerson = activitiesActivityScheduledInstanceForPerson.map { it.toActivityScheduledInstanceForPrisoner() }

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("should return scheduled instances for a prisoner") {
        whenever(activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null)).thenReturn(Response(data = activitiesActivityScheduledInstanceForPerson))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)
        result.data.shouldBe(activityScheduledInstanceForPerson)
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

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = errors))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)
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
        whenever(activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null)).thenReturn(Response(data = null, errors = errors))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
