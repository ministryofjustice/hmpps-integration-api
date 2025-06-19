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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.ActivitiesController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ActivitiesQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAttendanceReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [ActivitiesController::class])
@ActiveProfiles("test")
class ActivitiesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getActivitiesScheduleService: GetActivitiesScheduleService,
  @MockitoBean val getAttendanceReasonsService: GetAttendanceReasonsService,
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

      describe("/v1/activities/attendance-reasons") {
        val path = "$basePath/attendance-reasons"
        val reasonForAttendance =
          ReasonForAttendance(
            id = 1L,
            code = "SICK",
            description = "Unwell - unable to attend",
            attended = false,
            notes = "Reported sick by cellmate.",
          )

        it("should return 200 when successful") {
          whenever(getAttendanceReasonsService.execute())
            .thenReturn(Response(data = listOf(reasonForAttendance)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<List<ReasonForAttendance>>>()
            .shouldBe(DataResponse(data = listOf(reasonForAttendance)))
        }

        it("should call the audit service") {
          whenever(getAttendanceReasonsService.execute())
            .thenReturn(Response(data = listOf(reasonForAttendance)))

          mockMvc.performAuthorised(path)

          verify(auditService, times(1)).createEvent(
            "GET_ATTENDANCE_REASONS",
            mapOf(),
          )
        }

        it("returns 403 when getAttendanceReasonsService returns forbidden") {
          whenever(getAttendanceReasonsService.execute())
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.FORBIDDEN,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(403)
        }
      }
    },
  )
