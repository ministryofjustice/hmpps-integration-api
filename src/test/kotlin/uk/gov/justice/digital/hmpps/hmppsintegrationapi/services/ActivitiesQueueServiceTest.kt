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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleAllocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleAllocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleDetailed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleInstance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduleSuspension
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddCaseNoteRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Attendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.EarliestReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Exclusion
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedWaitingListApplications
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerAllocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerDeallocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Slot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

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
  @MockitoBean private val getPrisonPayBandsService: GetPrisonPayBandsService,
  @MockitoBean private val getWaitingListApplicationsService: GetWaitingListApplicationsService,
  @MockitoBean private val activitiesGateway: ActivitiesGateway,
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

      describe("Prisoner allocation") {
        val scheduleId = 123456L
        val prisonerNumber = "A1234AA"
        val activitiesActivityScheduleDetailed =
          ActivitiesActivityScheduleDetailed(
            id = scheduleId,
            instances = emptyList(),
            allocations =
              listOf(
                ActivitiesActivityScheduleAllocation(
                  id = 1L,
                  prisonerNumber = prisonerNumber,
                  bookingId = 10001L,
                  activitySummary = "Basic Education",
                  activityId = 2001L,
                  scheduleId = scheduleId,
                  scheduleDescription = "Morning Education",
                  isUnemployment = false,
                  prisonPayBand = null,
                  startDate = LocalDate.now().toString(),
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
                  exclusions = emptyList(),
                ),
              ),
            description = "Maths Level 1",
            suspensions = emptyList(),
            internalLocation = null,
            capacity = 10,
            activity =
              ActivitiesActivity(
                id = 2001L,
                prisonCode = "MDI",
                attendanceRequired = false,
                inCell = false,
                onWing = false,
                offWing = false,
                pieceWork = false,
                outsideWork = false,
                payPerSession = "H",
                summary = "Maths class",
                description = null,
                category = ActivitiesActivityCategory(id = 1L, code = "EDUCATION", name = "Education", description = "Educational activities"),
                riskLevel = "low",
                minimumEducationLevel = emptyList(),
                endDate = LocalDate.now().plusMonths(1).toString(),
                capacity = 10,
                allocated = 1,
                createdTime = LocalDateTime.now(),
                activityState = "LIVE",
                paid = true,
              ),
            scheduleWeeks = 2,
            slots =
              listOf(
                ActivitiesSlot(
                  id = 1L,
                  timeSlot = "AM",
                  weekNumber = 1,
                  startTime = "09:00",
                  endTime = "11:00",
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
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().plusMonths(6).toString(),
            runsOnBankHoliday = false,
            updatedTime = null,
            updatedBy = null,
            usePrisonRegimeTime = true,
          )

        val prisonPayBand =
          listOf(
            PrisonPayBand(
              id = 1L,
              alias = "pay band",
              description = "pay band description",
            ),
          )

        val paginatedWaitingListApplications =
          PaginatedWaitingListApplications(
            content =
              listOf(
                WaitingListApplication(
                  id = 1L,
                  activityId = 100L,
                  scheduleId = 200L,
                  allocationId = null,
                  prisonId = "MDI",
                  prisonerNumber = "A1234AA",
                  bookingId = 300L,
                  status = "PENDING",
                  statusUpdatedTime = null,
                  requestedDate = LocalDate.now(),
                  comments = null,
                  declinedReason = null,
                  creationTime = LocalDateTime.now(),
                  updatedTime = null,
                  earliestReleaseDate =
                    EarliestReleaseDate(
                      releaseDate = LocalDate.now().plusMonths(6).toString(),
                      isTariffDate = false,
                      isIndeterminateSentence = false,
                      isImmigrationDetainee = false,
                      isConvictedUnsentenced = false,
                      isRemand = false,
                    ),
                  nonAssociations = null,
                ),
              ),
            totalPages = 1,
            totalCount = 1L,
            isLastPage = true,
            count = 1,
            page = 1,
            perPage = 10,
          )

        beforeTest {
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          whenever(getPrisonPayBandsService.execute(prisonId, filters))
            .thenReturn(Response(data = prisonPayBand))
        }
        it("Returns an error if allocation start date is in the past") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().minusMonths(1),
              endDate = LocalDate.now().plusMonths(1),
            )

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation start date must not be in the past")))
        }

        it("Returns an error if allocation start date is not the same as or before the end date") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now(),
            )

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation start date must be the same as or before the end date")))
        }

        it("Returns an error if scheduleInstanceId is not provided when allocation start date is today") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now(),
              endDate = LocalDate.now().plusMonths(1),
            )

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "scheduleInstanceId must be provided when allocation start date is today")))
        }

        it("Returns an error if exclusion start time is after custom end time") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusDays(1),
              endDate = LocalDate.now().plusMonths(1),
              exclusions =
                listOf(
                  Slot(
                    id = 101L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "12:00",
                    endTime = "09:00",
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
            )

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Exclusion start time cannot be after custom end time")))
        }

        it("Returns an error if the activities gateway returns an error") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(2),
            )

          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "error from activities gateway")
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error when the allocation does not have a pay band when the activity is paid") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(2),
            )

          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation must have a pay band when the activity is paid")
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error when the allocation has a pay band when the activity is not paid") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(2),
              payBandId = 1L,
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(activity = activitiesActivityScheduleDetailed.activity.copy(paid = false))
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation cannot have a pay band when the activity is unpaid")
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if the prisonPayBandsService returns an error") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(2),
              payBandId = 1L,
            )

          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "error from prisonPayBandsService")
          whenever(getPrisonPayBandsService.execute(prisonId, filters))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if the prisoner allocation start date is before the schedule start date") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(3),
              payBandId = 1L,
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(startDate = LocalDate.now().plusMonths(2).toString(), allocations = emptyList())
          val scheduleStart = LocalDate.parse(activitiesActivityScheduleDetailed.startDate)
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation start date must not be before the activity schedule start date ($scheduleStart)")
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if the prisoner allocation end date is after the schedule end date") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              endDate = LocalDate.now().plusMonths(7),
              payBandId = 1L,
            )

          val scheduleEnd = LocalDate.parse(activitiesActivityScheduleDetailed.endDate)
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Allocation end date must not be after the activity schedule end date ($scheduleEnd)")

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if prisoner is already allocated and status is not ENDED") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
            )

          val description = activitiesActivityScheduleDetailed.description
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Prisoner with $prisonerNumber is already allocated to schedule $description.")

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if no matching slot exists for an exclusion") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "PM",
                    weekNumber = 2,
                    startTime = "09:00",
                    endTime = "11:00",
                    daysOfWeek = listOf("Mon", "Tue", "Wed"),
                    mondayFlag = true,
                    tuesdayFlag = false,
                    wednesdayFlag = true,
                    thursdayFlag = false,
                    fridayFlag = false,
                    saturdayFlag = false,
                    sundayFlag = false,
                  ),
                ),
            )

          val exclusion = allocationRequest.exclusions?.get(0)
          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(allocations = emptyList())
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "No ${exclusion?.timeSlot} slots in week number ${exclusion?.weekNumber} for schedule $scheduleId")
          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if getWaitingListApplicationsService returns an error") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "09:00",
                    endTime = "11:00",
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
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(allocations = emptyList())
          val pendingWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("PENDING"))
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "error from getWaitingListApplicationsService")

          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          whenever(getWaitingListApplicationsService.execute(prisonId, pendingWaitingListSearchRequest, filters))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if prisoner has a PENDING waiting list application") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "09:00",
                    endTime = "11:00",
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
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(allocations = emptyList())
          val pendingWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("PENDING"))
          val approvedWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("APPROVED"))
          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.CONFLICT, "Prisoner has a PENDING waiting list application. It must be APPROVED before they can be allocated.")

          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          whenever(getWaitingListApplicationsService.execute(prisonId, pendingWaitingListSearchRequest, filters))
            .thenReturn(Response(data = paginatedWaitingListApplications))

          whenever(getWaitingListApplicationsService.execute(prisonId, approvedWaitingListSearchRequest, filters))
            .thenReturn(Response(data = null))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("Returns an error if prisoner has more than one APPROVED waiting list application") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "09:00",
                    endTime = "11:00",
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
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(allocations = emptyList())
          val pendingWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("PENDING"))
          val approvedWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("APPROVED"))
          val pendingResponse = paginatedWaitingListApplications.copy(content = emptyList(), totalPages = 0, totalCount = 0L, count = 0)
          val approvedResponse =
            paginatedWaitingListApplications.copy(
              content =
                listOf(
                  paginatedWaitingListApplications.content.first().copy(status = "APPROVED"),
                  paginatedWaitingListApplications.content.first().copy(id = 2L, status = "APPROVED"),
                ),
            )

          val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.CONFLICT, "Prisoner has more than one APPROVED waiting list application. A prisoner can only have one approved waiting list application.")

          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          whenever(getWaitingListApplicationsService.execute(prisonId, pendingWaitingListSearchRequest, filters))
            .thenReturn(Response(data = pendingResponse))

          whenever(getWaitingListApplicationsService.execute(prisonId, approvedWaitingListSearchRequest, filters))
            .thenReturn(Response(data = approvedResponse))

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeNull()
          result.errors.shouldBe(listOf(error))
        }

        it("successfully adds to message queue") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "09:00",
                    endTime = "11:00",
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
            )

          val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(allocations = emptyList())
          val pendingWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("PENDING"))
          val approvedWaitingListSearchRequest = WaitingListSearchRequest(prisonerNumbers = listOf(prisonerNumber), status = listOf("APPROVED"))
          val response = paginatedWaitingListApplications.copy(content = emptyList(), totalPages = 0, totalCount = 0L, count = 0)

          whenever(activitiesGateway.getActivityScheduleById(scheduleId))
            .thenReturn(Response(data = activitiesActivityScheduleDetailed))

          whenever(getWaitingListApplicationsService.execute(prisonId, pendingWaitingListSearchRequest, filters))
            .thenReturn(Response(data = response))

          whenever(getWaitingListApplicationsService.execute(prisonId, approvedWaitingListSearchRequest, filters))
            .thenReturn(Response(data = response))
          val messageBody = """{"messageId": "1", "eventType": "AllocatePrisonerFromActivitySchedule", "messageAttributes": {}, who: "$who"}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()
          result.data.message.shouldBe("Prisoner allocation written to queue")
          result.errors.shouldBeEmpty()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }

        it("successfully adds test message to message queue") {
          val allocationRequest =
            PrisonerAllocationRequest(
              prisonerNumber = prisonerNumber,
              startDate = LocalDate.now().plusMonths(1),
              payBandId = 1L,
              exclusions =
                listOf(
                  Slot(
                    id = 1L,
                    timeSlot = "AM",
                    weekNumber = 1,
                    startTime = "09:00",
                    endTime = "11:00",
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
              testEvent = "TestEvent",
            )
          val messageBody = """{"messageId": "1", "eventType": "TestEvent", "messageAttributes": {}}"""
          whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

          val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
          result.data.shouldBeTypeOf<HmppsMessageResponse>()

          verify(mockSqsClient).sendMessage(
            argThat<SendMessageRequest> { request: SendMessageRequest? ->
              request?.queueUrl() == "https://test-queue-url" &&
                request.messageBody() == messageBody
            },
          )
        }
      }
    },
  )
