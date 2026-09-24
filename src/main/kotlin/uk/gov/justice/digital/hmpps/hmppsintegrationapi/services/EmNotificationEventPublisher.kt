package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Clock
import java.time.OffsetDateTime

private val log = KotlinLogging.logger {}

@Component
class EmNotificationEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val clock: Clock = Clock.systemUTC(),
) {
  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("emnotificationevents") as HmppsTopic }

  fun publish(
    caseId: String,
    statusUpdate: CaseStatusUpdate,
  ) {
    val eventId = eventId(caseId, statusUpdate)
    val event =
      EmNotificationEvent(
        eventId = eventId,
        publishedAt = OffsetDateTime.now(clock),
        data =
          CaseStatusReturned(
            caseId = caseId,
            status = statusUpdate.status.value,
            reasons = statusUpdate.reasons,
            datetimeOfStatusChange = statusUpdate.datetimeOfStatusChange.toString(),
          ),
      )

    log.info("Publishing EM notification event ${event.eventType} with id $eventId")
    eventTopic.snsClient
      .publish(
        PublishRequest
          .builder()
          .topicArn(eventTopic.arn)
          .message(objectMapper.writeValueAsString(event))
          .messageGroupId(messageGroupId(caseId))
          .messageDeduplicationId(eventId)
          .eventTypeMessageAttributes(event.eventType)
          .build(),
      ).get()
  }

  data class EmNotificationEvent(
    val eventId: String,
    val publishedAt: OffsetDateTime,
    val data: CaseStatusReturned,
    val version: Int = 1,
    val source: String = SOURCE,
    val eventType: String = EVENT_TYPE,
  ) {
    companion object {
      const val EVENT_TYPE = "electronic-monitoring.case-status-returned"
      const val SOURCE = "hmpps-external-api"
    }
  }

  data class CaseStatusReturned(
    val caseId: String,
    val status: String,
    val reasons: List<CaseStatusReason>?,
    val datetimeOfStatusChange: String,
  )

  private fun eventId(
    caseId: String,
    statusUpdate: CaseStatusUpdate,
  ): String {
    val canonicalPayload =
      objectMapper.writeValueAsString(
        mapOf(
          "caseId" to caseId,
          "status" to statusUpdate.status.value,
          "reasons" to statusUpdate.reasons,
          "datetimeOfStatusChange" to statusUpdate.datetimeOfStatusChange.toString(),
        ),
      )
    val digest = MessageDigest.getInstance("SHA-256").digest(canonicalPayload.toByteArray(StandardCharsets.UTF_8))
    return "sha256:${digest.joinToString("") { "%02x".format(it) }}"
  }

  private fun messageGroupId(caseId: String): String {
    val digest = MessageDigest.getInstance("SHA-256").digest(caseId.toByteArray(StandardCharsets.UTF_8))
    return "case:${digest.joinToString("") { "%02x".format(it) }}"
  }
}
