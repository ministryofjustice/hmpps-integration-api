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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddCaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeallocationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PayRate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReasonForAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SuitabilityCriteria
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ActivitiesQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesScheduleService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetActivitiesSuitabilityCriteriaService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAttendanceReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetDeallocationReasonsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduleDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.DayOfWeek
import java.time.LocalDate

@WebMvcTest(controllers = [ActivitiesController::class])
@ActiveProfiles("test")
class ActivitiesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getActivitiesScheduleService: GetActivitiesScheduleService,
  @MockitoBean val getAttendanceReasonsService: GetAttendanceReasonsService,
  @MockitoBean val getDeallocationReasonsService: GetDeallocationReasonsService,
  @MockitoBean val getScheduleDetailsService: GetScheduleDetailsService,
  @MockitoBean val activitiesQueueService: ActivitiesQueueService,
  @MockitoBean val getActivitiesSuitabilityCriteriaService: GetActivitiesSuitabilityCriteriaService,
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
            id = 1001L,
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
                  id = scheduleId,
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
                        daysOfWeek = setOf(DayOfWeek.SUNDAY),
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

      describe("GET /schedule/{scheduleId}/suitability-criteria") {
        val scheduleId = 123456L
        val filters = null
        val path = "$basePath/schedule/$scheduleId/suitability-criteria"
        val suitabilityCriteria =
          SuitabilityCriteria(
            riskLevel = "medium",
            isPaid = true,
            payRate =
              PayRate(
                incentiveCode = "BAS",
                incentiveLevel = "Basic",
                prisonPayBand =
                  PrisonPayBand(
                    id = 123456L,
                    alias = "Low",
                    description = "Pay band 1",
                  ),
                rate = 150,
                pieceRate = 150,
                pieceRateItems = 10,
              ),
            minimumEducationLevel =
              listOf(
                MinimumEducationLevel(
                  educationLevelCode = "Basic",
                  educationLevelDescription = "Basic",
                  studyAreaCode = "ENGLA",
                  studyAreaDescription = "English Language",
                ),
              ),
          )

        beforeEach {
          Mockito.reset(getActivitiesSuitabilityCriteriaService)
        }

        it("should return 200 when success") {
          whenever(getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters))
            .thenReturn(Response(data = suitabilityCriteria))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<SuitabilityCriteria>>()
            .shouldBe(DataResponse(data = suitabilityCriteria))
        }

        it("should call the audit service") {
          whenever(getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters))
            .thenReturn(Response(data = suitabilityCriteria))

          mockMvc.performAuthorised(path)

          verify(auditService, times(1)).createEvent(
            "GET_ACTIVITY_SCHEDULE_SUITABILITY_CRITERIA",
            mapOf("scheduleId" to scheduleId.toString()),
          )
        }

        it("returns 400 when getActivitiesSuitabilityCriteriaService returns bad request") {
          whenever(getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters))
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

        it("returns 404 when getActivitiesSuitabilityCriteriaService returns not found") {
          whenever(getActivitiesSuitabilityCriteriaService.execute(scheduleId, filters))
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

      describe("GET /v1/activities/attendance-reasons") {
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

      describe("PUT /v1/activities/schedule/{scheduleId}/deallocate") {
        val scheduleId = 123456L
        val path = "$basePath/schedule/$scheduleId/deallocate"
        val who = "automated-test-client"
        var filters = null
        val prisonerDeallocationRequest =
          PrisonerDeallocationRequest(
            prisonerNumber = "A1234AA",
            reasonCode = "RELEASED",
            endDate = LocalDate.now(),
            caseNote =
              AddCaseNoteRequest(
                type = "GEN",
                text = "Case note text",
              ),
            scheduleInstanceId = 1234L,
          )

        it("should return 200 when success") {
          whenever(activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Prisoner deallocation written to queue")))

          val result = mockMvc.performAuthorisedPut(path, prisonerDeallocationRequest)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<HmppsMessageResponse>>()
            .shouldBe(DataResponse(data = HmppsMessageResponse("Prisoner deallocation written to queue")))
        }

        it("should call the audit service") {
          whenever(activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Prisoner deallocation written to queue")))

          mockMvc.performAuthorisedPut(path, prisonerDeallocationRequest)

          verify(auditService, times(1)).createEvent(
            "PUT_DEALLOCATE_PRISONER_FROM_ACTIVITY",
            mapOf("scheduleId" to scheduleId.toString()),
          )
        }

        it("returns 400 when activitiesQueueService returns bad request") {
          whenever(activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters))
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

          val result = mockMvc.performAuthorisedPut(path, prisonerDeallocationRequest)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when activitiesQueueService returns not found") {
          whenever(activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters))
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

          val result = mockMvc.performAuthorisedPut(path, prisonerDeallocationRequest)
          result.response.status.shouldBe(404)
        }
      }

      describe("GET /v1/activities/deallocation-reasons") {
        val path = "$basePath/deallocation-reasons"
        val deallocationReason =
          DeallocationReason(
            code = "RELEASED",
            description = "Released from prison",
          )

        it("should return 200 when successful") {
          whenever(getDeallocationReasonsService.execute())
            .thenReturn(Response(data = listOf(deallocationReason)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<List<DeallocationReason>>>()
            .shouldBe(DataResponse(data = listOf(deallocationReason)))
        }

        it("should call the audit service") {
          whenever(getDeallocationReasonsService.execute())
            .thenReturn(Response(data = listOf(deallocationReason)))

          mockMvc.performAuthorised(path)

          verify(auditService, times(1)).createEvent(
            "GET_DEALLOCATION_REASONS",
            mapOf(),
          )
        }

        it("returns 403 when getDeallocationReasonsService returns forbidden") {
          whenever(getDeallocationReasonsService.execute())
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

      describe("POST /v1/activities/schedule/{scheduleId}/allocate") {
        val scheduleId = 123456L
        val prisonerNumber = "A1234AA"
        val path = "$basePath/schedule/$scheduleId/allocate"
        val who = "automated-test-client"
        var filters = null
        val prisonerAllocationRequest =
          PrisonerAllocationRequest(
            prisonerNumber = prisonerNumber,
            startDate = LocalDate.now().plusMonths(1),
            payBandId = 1L,
            exclusions =
              listOf(
                Exclusion(
                  timeSlot = "AM",
                  weekNumber = 1,
                  customStartTime = "09:00",
                  customEndTime = "11:00",
                  daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
                  monday = true,
                  tuesday = true,
                  wednesday = true,
                  thursday = false,
                  friday = false,
                  saturday = false,
                  sunday = false,
                ),
              ),
          )

        it("should return 200 when success") {
          whenever(activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Prisoner allocation written to queue")))

          val result = mockMvc.performAuthorisedPost(path, prisonerAllocationRequest)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response
            .contentAsJson<DataResponse<HmppsMessageResponse>>()
            .shouldBe(DataResponse(data = HmppsMessageResponse("Prisoner allocation written to queue")))
        }

        it("should call the audit service") {
          whenever(activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, who, filters))
            .thenReturn(Response(data = HmppsMessageResponse("Prisoner allocation written to queue")))

          mockMvc.performAuthorisedPost(path, prisonerAllocationRequest)

          verify(auditService, times(1)).createEvent(
            "POST_ALLOCATE_PRISONER_TO_ACTIVITY",
            mapOf("scheduleId" to scheduleId.toString()),
          )
        }

        it("returns 400 when activitiesQueueService returns bad request") {
          whenever(activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, who, filters))
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

          val result = mockMvc.performAuthorisedPost(path, prisonerAllocationRequest)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when activitiesQueueService returns not found") {
          whenever(activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, who, filters))
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

          val result = mockMvc.performAuthorisedPost(path, prisonerAllocationRequest)
          result.response.status.shouldBe(404)
        }

        it("returns 409 when activitiesQueueService returns conflict") {
          whenever(activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, prisonerAllocationRequest, who, filters))
            .thenReturn(
              Response(
                data = null,
                errors =
                  listOf(
                    UpstreamApiError(
                      type = UpstreamApiError.Type.CONFLICT,
                      causedBy = UpstreamApi.ACTIVITIES,
                    ),
                  ),
              ),
            )

          val result = mockMvc.performAuthorisedPost(path, prisonerAllocationRequest)
          result.response.status.shouldBe(409)
        }
      }
    },
  )
