package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import software.amazon.awssdk.services.sqs.model.SendMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.sqs.SQSService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Test implementation of the hmpp
 *
 * @constructor
 * TODO
 *
 * @param queues
 * @param topics
 */

class TestSQSService(
  queues: List<String> = emptyList(),
  topics: List<String> = emptyList(),
) : SQSService {
  val snsClient = TestSnsAsyncClient()
  var hmppsQueues = queues.map { HmppsQueue(it, TestSqsAsyncClient("${it}Url"), it) }
  var hmppsTopics = topics.map { HmppsTopic(it, it, snsClient) }

  override fun findByTopicId(topicId: String): HmppsTopic = hmppsTopics.first { it.id == topicId }

  override fun findByQueueId(queueId: String): HmppsQueue = hmppsQueues.firstOrNull { it.id == queueId } ?: throw RuntimeException("cant find queue $queueId")
}

class TestSqsAsyncClient(
  val url: String,
) : SqsAsyncClient {
  var messages = mutableListOf<String>()

  override fun serviceName(): String = "sqs"

  override fun close() {
    messages.clear()
  }

  override fun getQueueUrl(getQueueUrlRequest: GetQueueUrlRequest): CompletableFuture<GetQueueUrlResponse> =
    CompletableFuture.completedFuture(
      GetQueueUrlResponse
        .builder()
        .queueUrl(url)
        .build(),
    )

  override fun sendMessage(sendMessageRequest: SendMessageRequest): CompletableFuture<SendMessageResponse> {
    messages.add(sendMessageRequest.messageBody())
    return CompletableFuture.completedFuture(
      SendMessageResponse
        .builder()
        .messageId(UUID.randomUUID().toString())
        .build(),
    )
  }

  fun purgeQueue() {
    messages.clear()
  }

  inline fun <reified T : Any> messagesOnQueue(): List<T> =
    messages.map {
      val messageContent = objectMapper.readTree(it)
      objectMapper.readValue(messageContent.get("Message").textValue(), T::class.java)
    }
}

class TestSnsAsyncClient : SnsAsyncClient {
  override fun serviceName(): String = "sns"

  override fun close() {}
}
