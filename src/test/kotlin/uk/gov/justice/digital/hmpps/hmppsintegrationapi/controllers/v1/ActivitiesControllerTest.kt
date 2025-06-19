package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleAllocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleInstance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleSuspension
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ActivitiesQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduleDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.DayOfWeek

@WebMvcTest(controllers = [ActivitiesController::class])
@ActiveProfiles("test")
class ActivitiesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getActivitiesScheduleService: GetActivitiesScheduleService,
  @MockitoBean val getScheduleDetailsService: GetScheduleDetailsService,
  @MockitoBean val activitiesQueueService: ActivitiesQueueService,
) : DescribeSpec(
    {
      val basePath = "/v1/activities"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      afterTest {
        Mockito.reset(auditService)
      }

      describe("GET /{activityId}/schedules") {
        val activityId = 123456L
        val filters = null
        val path = "$basePath/$activityId/schedules"

        val activitiesSchedule =
          ActivitySchedule(
            scheduleId = 1001L,
            description = "Morning Education Class",
            internalLocation =
              InternalLocation(
                code = "EDU-ROOM1",
                description = "Education Room 1",
              ),
            capacity = 25,
            scheduleWeeks = 2,
            slots =
              listOf(
                Slot(
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
          )

        beforeEach {
          Mockito.reset(getActivitiesScheduleService)
        }

        it("should return 200 when success") {
          whenever(getActivitiesScheduleService.execute(activityId, filters))
            .thenReturn(Response(data = listOf(activitiesSchedule)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<List<ActivitySchedule>>>()
            .shouldBe(DataResponse(data = listOf(activitiesSchedule)))
        }

        it("should call the audit service") {
          whenever(getActivitiesScheduleService.execute(activityId, filters))
            .thenReturn(Response(data = listOf(activitiesSchedule)))

          mockMvc.performAuthorised(path)

          verify(auditService, times(1)).createEvent(
            "GET_ACTIVITY_SCHEDULES",
            mapOf("activityId" to activityId.toString()),
          )
        }

        it("returns 400 when getActivitiesScheduleService returns bad request") {
          whenever(getActivitiesScheduleService.execute(activityId, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.BAD_REQUEST,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when getActivitiesScheduleService returns not found") {
          whenever(getActivitiesScheduleService.execute(activityId, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(404)
        }
      }

      describe("GET /schedule/{scheduleId}") {
        val scheduleId = 123456L
        val filters = null
        val path = "$basePath/schedule/$scheduleId"
        val activityScheduleDetailed =
          ActivityScheduleDetailed(
            instances =
              listOf(
                ActivityScheduleInstance(
                  scheduleInstanceId = scheduleId,
                  date = "2022-10-20",
                  startTime = "09:00",
                  endTime = "12:00",
                  timeSlot = "AM",
                  cancelled = false,
                  cancelledTime = null,
                  attendances =
                    listOf(
                      Attendance(
                        id = 123L,
                        scheduledInstanceId = scheduleId,
                        prisonerNumber = "A1234AA",
                        status = "ACTIVE",
                        editable = true,
                        payable = false,
                      ),
                    ),
                ),
              ),
            allocations =
              listOf(
                ActivityScheduleAllocation(
                  prisonerNumber = "A1234AA",
                  activitySummary = "Summary",
                  isUnemployment = true,
                  prisonPayBand =
                    PrisonPayBand(
                      id = 123456L,
                      alias = "pay band",
                      description = "pay band description",
                    ),
                  startDate = "2022-10-20",
                  endDate = null,
                  allocatedTime = null,
                  deallocatedTime = null,
                  deallocatedReason = null,
                  suspendedTime = null,
                  suspendedReason = null,
                  status = "ACTIVE",
                  plannedDeallocation = null,
                  plannedSuspension = null,
                  exclusions =
                    listOf(
                      Exclusion(
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
            suspensions =
              listOf(
                ActivityScheduleSuspension(
                  suspendedFrom = "2022-10-20",
                  suspendedUntil = "2022-10-21",
                ),
              ),
            description = "Morning Education Class",
            internalLocation = 123,
            capacity = 25,
            scheduleWeeks = 2,
            slots =
              listOf(
                Slot(
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
            runsOnBankHoliday = false,
          )

        beforeEach {
          Mockito.reset(getScheduleDetailsService)
        }

        it("should return 200 when success") {
          whenever(getScheduleDetailsService.execute(scheduleId, filters))
            .thenReturn(Response(data = activityScheduleDetailed))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<ActivityScheduleDetailed>>()
            .shouldBe(DataResponse(data = activityScheduleDetailed))
        }

        it("should call the audit service") {
          whenever(getScheduleDetailsService.execute(scheduleId, filters))
            .thenReturn(Response(data = activityScheduleDetailed))

          mockMvc.performAuthorised(path)

          verify(auditService, times(1)).createEvent(
            "GET_SCHEDULE_DETAILS",
            mapOf("scheduleId" to scheduleId.toString()),
          )
        }

        it("returns 400 when getActivitiesScheduleService returns bad request") {
          whenever(getScheduleDetailsService.execute(scheduleId, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.BAD_REQUEST,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when getActivitiesScheduleService returns not found") {
          whenever(getScheduleDetailsService.execute(scheduleId, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(404)
        }
      }

      describe("PUT /v1/activities/schedule/attendance") {
        val path = "$basePath/schedule/attendance"
        val who = "automated-test-client"
        var filters = null
        val attendanceUpdateRequests =
          listOf(
            AttendanceUpdateRequest(
              id = 123456L,
              prisonId = "MDI",
              status = "WAITING",
              attendanceReason = "ATTENDED",
              comment = "Prisoner has COVID-19",
              issuePayment = true,
              caseNote = "Prisoner refused to attend the scheduled activity without reasonable excuse",
              incentiveLevelWarningIssued = true,
              otherAbsenceReason = "Prisoner has another reason for missing the activity",
            ),
            AttendanceUpdateRequest(
              id = 234567L,
              prisonId = "MDI",
              status = "WAITING",
              attendanceReason = "ATTENDED",
              comment = "Prisoner has COVID-19",
              issuePayment = true,
              caseNote = "Prisoner refused to attend the scheduled activity without reasonable excuse",
              incentiveLevelWarningIssued = true,
              otherAbsenceReason = "Prisoner has another reason for missing the activity",
            ),
          )

        it("should return 200 when success") {
          whenever(activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Attendance updated successfully")))

          val result = mockMvc.performAuthorisedPut(path, attendanceUpdateRequests)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<HmppsMessageResponse>>()
            .shouldBe(DataResponse(data = HmppsMessageResponse("Attendance updated successfully")))
        }

        it("should call the audit service") {
          whenever(activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Attendance updated successfully")))

          mockMvc.performAuthorisedPut(path, attendanceUpdateRequests)

          verify(auditService, times(1)).createEvent(
            "PUT_ATTENDANCE",
            mapOf("attendanceIds" to "123456, 234567"),
          )
        }

        it("returns 400 when activitiesQueueService returns bad request") {
          whenever(activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests, who, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.BAD_REQUEST,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorisedPut(path, attendanceUpdateRequests)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when activitiesQueueService returns not found") {
          whenever(activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests, who, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorisedPut(path, attendanceUpdateRequests)
          result.response.status.shouldBe(404)
        }
      }
    },
  )
