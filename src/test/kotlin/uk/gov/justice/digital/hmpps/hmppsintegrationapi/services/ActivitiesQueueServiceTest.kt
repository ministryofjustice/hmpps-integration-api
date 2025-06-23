package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleAllocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleInstance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleSuspension
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddCaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.DayOfWeek
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesQueueService::class],
)
class ActivitiesQueueServiceTest(
  private val activitiesQueueService: ActivitiesQueueService,
  @MockitoBean val hmppsQueueService: HmppsQueueService,
  @MockitoBean val objectMapper: ObjectMapper,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val getAttendanceByIdService: GetAttendanceByIdService,
  @MockitoBean private val getScheduleDetailsService: GetScheduleDetailsService,
) : DescribeSpec(
    {
      val mockSqsClient = mock<SqsAsyncClient>()
      val activitiesQueue =
        mock<HmppsQueue> {
          on { sqsClient } doReturn mockSqsClient
          on { queueUrl } doReturn "https://test-queue-url"
        }

      val prisonId = "MDI"
      val filters = ConsumerFilters(prisons = listOf(prisonId))
      val who = "automated-test-client"

      beforeTest {
        reset(mockSqsClient, objectMapper, consumerPrisonAccessService)

        whenever(hmppsQueueService.findByQueueId("activities")).thenReturn(activitiesQueue)
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
          .thenReturn(Response(data = null, errors = emptyList()))
      }

      describe("Mark prisoner attendance") {
        val attendanceUpdateRequests =
          listOf(
            AttendanceUpdateRequest(
              id = 123456L,
              prisonId = prisonId,
              status = "WAITING",
              attendanceReason = "SICK",
              comment = "Prisoner ill",
              issuePayment = true,
              caseNote = "case note",
              incentiveLevelWarningIssued = false,
              otherAbsenceReason = "other reason",
            ),
          )

        it("successfully adds to message queue") {
          val messageBody = """{"messageId": "1", "eventType": "MarkPrisonerAttendance", "messageAttributes": {}, who: "$who"}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)
          whenever(getAttendanceByIdService.execute(attendanceUpdateRequests[0].id, filters))
            .thenReturn(Response(data = Attendance(id = 123456L, scheduledInstanceId = 1L, prisonerNumber = "A1234AA", status = "WAITING", editable = true, payable = true)))

          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests = attendanceUpdateRequests, who = who, filters = filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()
          result.data.message.shouldBe("Attendance update written to queue")
          result.errors.shouldBeEmpty()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }

        it("should send attendance update test request") {
          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests.map { it.copy(status = "TestEvent") }, who, filters)
          result.data?.message.shouldBe("Attendance update written to queue")
          result.errors.shouldBeEmpty()

          verify(consumerPrisonAccessService, never()).checkConsumerHasPrisonAccess<Any>(any(), any(), any())
        }

        it("successfully adds test message to message queue") {
          val messageBody = """{"messageId": "1", "eventType": "TestEvent", "messageAttributes": {}}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests.map { it.copy(status = "TestEvent") }, who, filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }

        it("should return errors when consumer does not have access to the prison") {
          val error = UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests = attendanceUpdateRequests, who = who, filters = filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(error))
        }

        it("should return errors when getAttendanceByIdService returns errors") {
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")
          whenever(getAttendanceByIdService.execute(attendanceUpdateRequests[0].id, filters))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests = attendanceUpdateRequests, who = who, filters = filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(error))
        }
      }

      describe("Prisoner deallocation") {
        val scheduleId = 123456L
        val prisonerNumber = "A1234AA"
        val prisonerDeallocationRequest =
          PrisonerDeallocationRequest(
            prisonerNumber = prisonerNumber,
            reasonCode = "RELEASED",
            endDate = LocalDate.now(),
            caseNote =
              AddCaseNoteRequest(
                type = "GEN",
                text = "Case note text",
              ),
            scheduleInstanceId = 1234L,
          )
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
                  prisonerNumber = prisonerNumber,
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
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().plusMonths(1).toString(),
            usePrisonRegimeTime = true,
            runsOnBankHoliday = false,
          )

        it("successfully adds to message queue") {
          val messageBody = """{"messageId": "1", "eventType": "DeallocatePrisonerFromActivitySchedule", "messageAttributes": {}, who: "$who"}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = activityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()
          result.data.message.shouldBe("Prisoner deallocation written to queue")
          result.errors.shouldBeEmpty()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }

        it("successfully adds test message to message queue") {
          val messageBody = """{"messageId": "1", "eventType": "TestEvent", "messageAttributes": {}}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest.copy(reasonCode = "TestEvent"), who, filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }

        it("returns an error if getScheduleDetailsService has an error") {
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "error from getScheduleDetailsService")
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("returns an error if getScheduleDetailsService returns data with no allocations") {
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = activityScheduleDetailed.copy(allocations = emptyList())))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Allocations not found for prisoner: $prisonerNumber")))
        }

        it("returns an error if getScheduleDetailsService returns data with no allocations for the prisoner") {
          val scheduleWithInvalidAllocations = activityScheduleDetailed.copy(allocations = listOf(activityScheduleDetailed.allocations[0].copy(prisonerNumber = "invalidPrisonerNumber")))
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = scheduleWithInvalidAllocations))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Allocations not found for prisoner: $prisonerNumber")))
        }

        it("returns an error if getScheduleDetailsService returns data with no active allocations for the prisoner") {
          val scheduleWithInvalidAllocations = activityScheduleDetailed.copy(allocations = listOf(activityScheduleDetailed.allocations[0].copy(status = "ENDED")))
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = scheduleWithInvalidAllocations))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, prisonerDeallocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Allocations not found for prisoner: $prisonerNumber")))
        }

        it("returns an error if passed in end date is after the date of the schedule") {
          val invalidPrisonerDeallocationRequest = prisonerDeallocationRequest.copy(endDate = LocalDate.parse(activityScheduleDetailed.endDate).plusDays(1))
          whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = activityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerDeallocationRequest(scheduleId, invalidPrisonerDeallocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Passed in end date cannot be after the end date of the schedule: ${activityScheduleDetailed.endDate}")))
        }
      }
    },
  )
