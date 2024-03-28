package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestAttributes
import org.springframework.web.context.request.RequestContextHolder
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsAuditEvent
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Service
@Component
class AuditService(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
  private val ser: AuthoriseConsumerService,
) {
  private val auditQueue by lazy { hmppsQueueService.findByQueueId("audit") as HmppsQueue }
  private val auditSqsClient by lazy { auditQueue.sqsClient }
  private val auditQueueUrl by lazy { auditQueue.queueUrl }

  fun createEvent(
    what: String,
    detail: String,
  ) {
    val username =
      RequestContextHolder.currentRequestAttributes()
        .getAttribute("clientName", RequestAttributes.SCOPE_REQUEST) as String

    auditSqsClient.sendMessage(
      SendMessageRequest.builder()
        .queueUrl(auditQueueUrl)
        .messageBody(
          objectMapper.writeValueAsString(
            HmppsAuditEvent(
              what = what,
              details = detail,
              who = username,
            ),
          ),
        )
        .build(),
    )
  }
}
