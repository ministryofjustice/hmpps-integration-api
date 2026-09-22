package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cemo.CemoOrderCaseSearchResult
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.up3.CaseStatusUpdate
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

private val log = KotlinLogging.logger {}

@Component
class EmNotificationEventPublisher(
  private val hmppsQueueService: HmppsQueueService,
  private val objectMapper: ObjectMapper,
) {
  internal val eventTopic by lazy { hmppsQueueService.findByTopicId("emnotificationevents") as HmppsTopic }

  fun publish(
    caseId: String,
    statusUpdate: CaseStatusUpdate,
    order: CemoOrderCaseSearchResult,
  ) {
    val event =
      EmNotificationEvent(
        caseId = caseId,
        status = statusUpdate.status.value,
        reasons = statusUpdate.reasons,
        datetimeOfStatusChange = statusUpdate.datetimeOfStatusChange.toString(),
        order = order,
      )

    log.info("Publishing EM notification event ${event.eventType} for case $caseId")
    eventTopic.snsClient
      .publish(
        PublishRequest
          .builder()
          .topicArn(eventTopic.arn)
          .message(objectMapper.writeValueAsString(event))
          .eventTypeMessageAttributes(event.eventType)
          .build(),
      ).get()
  }

  data class EmNotificationEvent(
    val caseId: String,
    val status: String,
    val reasons: List<CaseStatusReason>?,
    val datetimeOfStatusChange: String,
    val order: CemoOrderCaseSearchResult,
    val eventType: String = EVENT_TYPE,
  ) {
    companion object {
      const val EVENT_TYPE = "em.case.status.updated"
    }
  }
}

