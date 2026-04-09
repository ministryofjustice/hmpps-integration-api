package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.sqs

import org.springframework.stereotype.Component
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic

interface SQSService {
  fun findByTopicId(topicId: String): HmppsTopic?

  fun findByQueueId(queueId: String): HmppsQueue?
}

@Component
class HmppsSqsService(
  private val hmppsQueueService: HmppsQueueService,
) : SQSService {
  override fun findByTopicId(topicId: String): HmppsTopic? = hmppsQueueService.findByTopicId(topicId)

  override fun findByQueueId(queueId: String): HmppsQueue? = hmppsQueueService.findByQueueId(queueId)
}
