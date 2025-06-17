package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AttendanceUpdateRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesQueueService::class],
)
class ActivitiesQueueServiceTest(
  private val activitiesQueueService: ActivitiesQueueService,
  @MockitoBean val hmppsQueueService: HmppsQueueService,
  @MockitoBean val objectMapper: ObjectMapper,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
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
        reset(mockSqsClient, objectMapper)

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

        it("should return errors when consumer does not have access to the prison") {
          val error = UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")
          whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
            .thenReturn(Response(data = null, errors = listOf(error)))

          val result = activitiesQueueService.sendAttendanceUpdateRequest(attendanceUpdateRequests = attendanceUpdateRequests, who = who, filters = filters)
          result.data.shouldBe(null)
          result.errors.shouldBe(listOf(error))
        }
      }
    },
  )
