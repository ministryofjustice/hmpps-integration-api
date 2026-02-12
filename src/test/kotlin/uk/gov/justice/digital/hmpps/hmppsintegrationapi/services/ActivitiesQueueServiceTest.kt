package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.Mockito
import org.mockito.kotlin.*
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues.QueueProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
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
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val getAttendanceByIdService: GetAttendanceByIdService,
  @MockitoBean private val getScheduleDetailsService: GetScheduleDetailsService,
  @MockitoBean private val getPrisonPayBandsService: GetPrisonPayBandsService,
  @MockitoBean private val getWaitingListApplicationsByScheduleIdService: GetWaitingListApplicationsByScheduleIdService,
  @MockitoBean private val activitiesGateway: ActivitiesGateway,
  @MockitoBean private val queueProvider: QueueProvider,
) : DescribeSpec(
  {
    val mockSqsClient = mock<SqsAsyncClient>()
    val activitiesQueue =
      mock<HmppsQueue> {
        on { sqsClient } doReturn mockSqsClient
        on { queueUrl } doReturn "https://test-queue-url"
      }

    val prisonId = "MDI"
    val prisonerNumber = "A1234AA"
    val filters = ConsumerFilters(prisons = listOf(prisonId))
    val who = "automated-test-client"

    val persona = personInProbationAndNomisPersona
    val person =
      PersonInPrison(
        firstName = persona.firstName,
        lastName = persona.lastName,
        identifiers = persona.identifiers,
        prisonId = prisonId,
        youthOffender = false,
      )

    beforeTest {
      reset(mockSqsClient, objectMapper, consumerPrisonAccessService)

      whenever(hmppsQueueService.findByQueueId("activities")).thenReturn(activitiesQueue)
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
        .thenReturn(Response(data = null, errors = emptyList()))
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES))
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
            incentiveLevelWarningIssued = false,
            otherAbsenceReason = "other reason",
          ),
        )

      it("successfully adds to message queue") {
        val messageBody = """{"messageId": "1", "eventType": "MarkPrisonerAttendance", "messageAttributes": {}, who: "$who"}"""
        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)
        whenever(getAttendanceByIdService.execute(attendanceUpdateRequests[0].id, filters))
          .thenReturn(Response(data = Attendance(id = 123456L, scheduledInstanceId = 1L, prisonerNumber, status = "WAITING", editable = true, payable = true)))

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
      val prisonerDeallocationRequest =
        PrisonerDeallocationRequest(
          prisonerNumber = prisonerNumber,
          reasonCode = "RELEASED",
          endDate = LocalDate.now(),
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
                      prisonerNumber = prisonerNumber,
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

      it("successfully adds to message queue when the schedule does not have an end date") {
        val messageBody = """{"messageId": "1", "eventType": "DeallocatePrisonerFromActivitySchedule", "messageAttributes": {}, who: "$who"}"""
        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)
        whenever(getScheduleDetailsService.execute(scheduleId, filters)).thenReturn(Response(data = activityScheduleDetailed.copy(endDate = null)))

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
      val scheduledInstanceId = 1L
      val allocationRequest =
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

      val activitiesActivityScheduleDetailed =
        ActivitiesActivityScheduleDetailed(
          id = scheduleId,
          instances =
            listOf(
              ActivitiesActivityScheduleInstance(
                id = scheduledInstanceId,
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
          allocations = emptyList(),
          description = "Maths Level 1",
          suspensions = emptyList(),
          internalLocation = null,
          capacity = 10,
          activity =
            ActivitiesActivity(
              id = 2001L,
              prisonCode = prisonId,
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

      val pendingWaitingApplication =
        WaitingListApplication(
          id = 1L,
          activityId = 100L,
          scheduleId = 200L,
          allocationId = null,
          prisonId = prisonId,
          prisonerNumber = prisonerNumber,
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
        )

      beforeTest {
        Mockito.reset(getPersonService, activitiesGateway, getPrisonPayBandsService)

        whenever(getPersonService.getPrisoner(prisonerNumber, filters)).thenReturn(
          Response(data = person),
        )

        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed))

        whenever(getPrisonPayBandsService.execute(prisonId, filters))
          .thenReturn(Response(data = prisonPayBand))

        whenever(getWaitingListApplicationsByScheduleIdService.execute(scheduleId, filters))
          .thenReturn(Response(data = emptyList()))
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
            payBandId = 1L,
          )

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "scheduleInstanceId must be provided when allocation start date is today")))
      }

      it("Returns an error if scheduleInstanceId is provided there are no instances on the schedule") {
        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed.copy(instances = emptyList())))

        val allocationRequest =
          PrisonerAllocationRequest(
            prisonerNumber = prisonerNumber,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(1),
            payBandId = 1L,
            scheduleInstanceId = scheduledInstanceId,
          )

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "scheduleInstanceId not found on schedule")))
      }

      it("Returns an error if scheduleInstanceId is provided but not in the list of instances on the schedule") {
        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed.copy(instances = activitiesActivityScheduleDetailed.instances.map { it.copy(id = it.id + 1) })))

        val allocationRequest =
          PrisonerAllocationRequest(
            prisonerNumber = prisonerNumber,
            startDate = LocalDate.now(),
            endDate = LocalDate.now().plusMonths(1),
            payBandId = 1L,
            scheduleInstanceId = scheduledInstanceId,
          )

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "scheduleInstanceId not found on schedule")))
      }

      it("Returns an error if exclusion start time is after custom end time") {
        val allocationRequest =
          PrisonerAllocationRequest(
            prisonerNumber = prisonerNumber,
            startDate = LocalDate.now().plusDays(1),
            endDate = LocalDate.now().plusMonths(1),
            exclusions =
              listOf(
                Exclusion(
                  timeSlot = "AM",
                  weekNumber = 1,
                  customStartTime = "11:00",
                  customEndTime = "09:00",
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

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Exclusion start time cannot be after custom end time")))
      }

      it("Returns an error if the getPersonService returns an error") {
        val errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "error from getPersonService"))
        whenever(getPersonService.getPrisoner(prisonerNumber, filters))
          .thenReturn(Response(data = null, errors))

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
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

      it("Returns an error if the activities gateway returns an schedule belonging to a different prison to the prisoner") {
        whenever(getPersonService.getPrisoner(prisonerNumber, filters))
          .thenReturn(Response(data = person.copy(prisonId = "XYZ")))

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "Unable to allocate prisoner with prisoner number $prisonerNumber, prisoner is not active at prison ${activitiesActivityScheduleDetailed.activity.prisonCode}.")))
      }

      it("should return errors when consumer does not have access to the prison for the schedule") {
        val errors = listOf(UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES))
          .thenReturn(Response(data = null, errors))

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
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

        val activitiesActivityScheduleDetailed = activitiesActivityScheduleDetailed.copy(startDate = LocalDate.now().plusMonths(2).toString())
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

        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(
            Response(
              data =
                activitiesActivityScheduleDetailed.copy(
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
                ),
            ),
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
                Exclusion(
                  timeSlot = "AM",
                  weekNumber = 1,
                  customStartTime = "09:00",
                  customEndTime = "11:00",
                  daysOfWeek = setOf(DayOfWeek.SATURDAY),
                  monday = false,
                  tuesday = false,
                  wednesday = false,
                  thursday = true,
                  friday = false,
                  saturday = false,
                  sunday = false,
                ),
              ),
          )

        val exclusion = allocationRequest.exclusions?.get(0)
        val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "No ${exclusion?.timeSlot} slots in week number ${exclusion?.weekNumber} for schedule $scheduleId")

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(error))
      }

      it("Returns an error if getWaitingListApplicationsService returns an error") {
        val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.BAD_REQUEST, "error from getWaitingListApplicationsService")

        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed))

        whenever(getWaitingListApplicationsByScheduleIdService.execute(scheduleId, filters))
          .thenReturn(Response(data = null, errors = listOf(error)))

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(error))
      }

      it("Returns an error if prisoner has a PENDING waiting list application") {
        val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.CONFLICT, "Prisoner has a PENDING waiting list application. It must be APPROVED before they can be allocated.")

        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed))

        whenever(getWaitingListApplicationsByScheduleIdService.execute(scheduleId, filters))
          .thenReturn(Response(data = listOf(pendingWaitingApplication)))

        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(error))
      }

      it("Returns an error if prisoner has more than one APPROVED waiting list application") {
        val approvedWaitingListApplications =
          listOf(
            pendingWaitingApplication.copy(status = "APPROVED"),
            pendingWaitingApplication.copy(id = 2L, status = "APPROVED"),
          )

        whenever(getWaitingListApplicationsByScheduleIdService.execute(scheduleId, filters))
          .thenReturn(Response(data = approvedWaitingListApplications))

        val error = UpstreamApiError(UpstreamApi.ACTIVITIES, UpstreamApiError.Type.CONFLICT, "Prisoner has more than one APPROVED waiting list application. A prisoner can only have one approved waiting list application.")
        val result = activitiesQueueService.sendPrisonerAllocationRequest(scheduleId, allocationRequest, who, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(listOf(error))
      }

      it("successfully adds to message queue") {
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

      it("successfully adds to message queue when the schedule does not have an end date") {
        whenever(activitiesGateway.getActivityScheduleById(scheduleId))
          .thenReturn(Response(data = activitiesActivityScheduleDetailed.copy(endDate = null)))

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
                Exclusion(
                  timeSlot = "AM",
                  weekNumber = 1,
                  monday = true,
                  tuesday = true,
                  wednesday = true,
                  thursday = false,
                  friday = false,
                  saturday = false,
                  sunday = false,
                  customStartTime = "09:00",
                  customEndTime = "11:00",
                  daysOfWeek = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY),
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
