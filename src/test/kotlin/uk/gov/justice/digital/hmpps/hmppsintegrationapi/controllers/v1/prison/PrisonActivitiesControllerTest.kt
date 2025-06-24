package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstancesForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAdvanceAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendee
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RunningActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonActivitiesService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetScheduledInstancesForPrisonerService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.SearchAppointmentsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [PrisonActivitiesController::class])
@ActiveProfiles("test")
class PrisonActivitiesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonActivitiesService: GetPrisonActivitiesService,
  @MockitoBean val getScheduledInstancesForPrisonerService: GetScheduledInstancesForPrisonerService,
  @MockitoBean val searchAppointmentsService: SearchAppointmentsService,
) : DescribeSpec(
    {
      val basePath = "/v1/prison"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      afterTest {
        Mockito.reset(auditService)
      }

      describe("GET /v1/prison/{prisonId}/activities") {
        val prisonId = "ABC"
        val filters = null
        val path = "$basePath/$prisonId/activities"

        val runningActivity =
          RunningActivity(
            id = 123456L,
            activityName = "Maths level 1",
            category =
              ActivityCategory(
                code = "LEISURE_SOCIAL",
                name = "Leisure and social",
                description = "Such as association, library time and social clubs, like music or art",
              ),
            capacity = 10,
            allocated = 2,
            waitlisted = 2,
            activityState = "LIVE",
          )

        beforeEach {
          Mockito.reset(getPrisonActivitiesService)
        }

        it("should return 200 when success") {
          whenever(getPrisonActivitiesService.execute(prisonId, filters)).thenReturn(Response(data = listOf(runningActivity)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<DataResponse<List<RunningActivity>>>().shouldBe(DataResponse(data = listOf(runningActivity)))
        }

        it("should call the audit service") {
          whenever(getPrisonActivitiesService.execute(prisonId, filters)).thenReturn(Response(data = listOf(runningActivity)))

          mockMvc.performAuthorised(path)
          verify(
            auditService,
            times(1),
          ).createEvent(
            "GET_PRISON_ACTIVITIES",
            mapOf("prisonId" to prisonId),
          )
        }

        it("returns 400 when getPrisonActivityService returns bad request") {
          whenever(getPrisonActivitiesService.execute(prisonId, filters)).thenReturn(
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

        it("returns 404 when getPrisonRegimeService returns not found") {
          whenever(getPrisonActivitiesService.execute(prisonId, filters)).thenReturn(
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

      describe("GET /v1/prison/{prisonCode}/{prisonerId}/scheduled-instances") {
        val prisonerId = "A1234AA"
        val prisonCode = "MKI"
        val filters = null
        val path = "$basePath/$prisonCode/$prisonerId/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10"

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
          Mockito.reset(getScheduledInstancesForPrisonerService)
        }

        it("should return 200 when success") {
          whenever(getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)).thenReturn(Response(data = activityScheduledInstanceForPerson))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<DataResponse<List<ActivityScheduledInstanceForPrisoner>?>>().shouldBe(DataResponse(data = activityScheduledInstanceForPerson))
        }

        it("should call the audit service") {
          whenever(getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)).thenReturn(Response(data = activityScheduledInstanceForPerson))

          mockMvc.performAuthorised(path)
          verify(
            auditService,
            times(1),
          ).createEvent(
            "GET_SCHEDULED_INSTANCES_FOR_PRISONER",
            mapOf("prisonCode" to prisonCode, "prisonerId" to prisonerId, "startDate" to "2022-09-10", "endDate" to "2023-09-10", "slot" to null, "cancelled" to null.toString()),
          )
        }

        it("returns 400 when getScheduledInstancesForPrisonerService returns bad request") {
          whenever(getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)).thenReturn(
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

        it("returns 404 when getScheduledInstancesForPrisonerService returns not found") {
          whenever(getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null, filters)).thenReturn(
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

      describe("POST /v1/prison/{prisonId}/appointments/search") {
        val prisonId = "ABC"
        val filters = null
        val path = "$basePath/$prisonId/appointments/search"
        val exampleRequest =
          AppointmentSearchRequest(
            appointmentType = "INDIVIDUAL",
            startDate = LocalDate.parse("2025-01-01"),
            endDate = LocalDate.parse("2025-01-31"),
            timeSlots = listOf("AM", "PM"),
            categoryCode = "GYMW",
            inCell = false,
            prisonerNumbers = listOf("A1234AA"),
          )

        val appointmentDetails =
          listOf(
            AppointmentDetails(
              appointmentType = "INDIVIDUAL",
              appointmentId = 123456,
              prisonId = "SKI",
              appointmentName = "string",
              attendees =
                listOf(
                  Attendee(
                    prisonerNumber = "A1234BC",
                  ),
                ),
              category =
                AppointmentCategory(
                  code = "CHAP",
                  description = "Chaplaincy",
                ),
              customName = "Meeting with the governor",
              internalLocation =
                InternalLocation(
                  code = "SKI",
                  description = "Chapel",
                ),
              inCell = false,
              startDate = "2025-06-12",
              startTime = "13:00",
              endTime = "13:30",
              timeSlot = "AM",
              isRepeat = false,
              sequenceNumber = 3,
              maxSequenceNumber = 6,
              isEdited = false,
              isCancelled = false,
              isExpired = false,
            ),
          )

        beforeEach {
          Mockito.reset(searchAppointmentsService)
        }

        it("should return 200 when success") {
          whenever(searchAppointmentsService.execute(prisonId, exampleRequest, filters)).thenReturn(Response(data = appointmentDetails))

          val result = mockMvc.performAuthorisedPost(path, exampleRequest)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<DataResponse<List<AppointmentDetails>>>().shouldBe(DataResponse(data = appointmentDetails))
        }

        it("should call the audit service") {
          whenever(searchAppointmentsService.execute(prisonId, exampleRequest, filters)).thenReturn(Response(data = appointmentDetails))

          mockMvc.performAuthorisedPost(path, exampleRequest)
          verify(
            auditService,
            times(1),
          ).createEvent(
            "SEARCH_APPOINTMENTS",
            mapOf("prisonId" to prisonId, "startDate" to exampleRequest.startDate.toString()),
          )
        }

        it("returns 400 when searchAppointmentsService returns bad request") {
          whenever(searchAppointmentsService.execute(prisonId, exampleRequest, filters)).thenReturn(
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

          val result = mockMvc.performAuthorisedPost(path, exampleRequest)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when searchAppointmentsService returns not found") {
          whenever(searchAppointmentsService.execute(prisonId, exampleRequest, filters)).thenReturn(
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

          val result = mockMvc.performAuthorisedPost(path, exampleRequest)
          result.response.status.shouldBe(404)
        }
      }
    },
  )
