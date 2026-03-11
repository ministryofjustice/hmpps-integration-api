package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.env.Environment
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesResponse
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicResponse
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.Subscription
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.AuthorisationConfigReader
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class SubscriptionFilterPolicyUpdaterTest {
  val fileManager = Mockito.spy(FileManager::class.java)!!
  val environment = mock(Environment::class.java)!!
  val queueService = mock(HmppsQueueService::class.java)!!
  val topic = mock(HmppsTopic::class.java)!!
  val snsClient = mock(SnsAsyncClient::class.java)!!
  val testQueue = mock(HmppsQueue::class.java)!!
  val subscriptionsByTopicResponse = mock(ListSubscriptionsByTopicResponse::class.java)!!
  val subscriptionAttributesResponse = mock(GetSubscriptionAttributesResponse::class.java)!!
  val telemetryService = mock(TelemetryService::class.java)!!

  lateinit var updater: SubscriptionFilterPolicyUpdater
  lateinit var testConfig: AuthorisationConfig
  lateinit var policyManager: SubscriptionFilterPolicyManager

  val testQueueSubscription =
    Subscription
      .builder()
      .subscriptionArn("subscription-arn1")
      .protocol("sqs")
      .endpoint("test_queue_arn")
      .build()!!

  val anotherQueueSubscription =
    Subscription
      .builder()
      .subscriptionArn("subscription-arn2")
      .protocol("sqs")
      .endpoint("a_different_queue_arn")
      .build()!!

  @BeforeEach
  fun setup() {
    Mockito.reset(environment)
    whenever(testQueue.queueArn).thenReturn("test_queue_arn")
    whenever(topic.arn).doReturn("event_topic_arn")
    whenever(topic.snsClient).doReturn(snsClient)
    whenever(queueService.findByTopicId(INTEGRATION_EVENT_TOPIC)).thenReturn(topic)
    whenever(queueService.findByQueueId("testqueue")).thenReturn(testQueue)
    whenever(environment.activeProfiles).thenReturn(arrayOf("integration-test"))
    whenever(snsClient.listSubscriptionsByTopic(any<ListSubscriptionsByTopicRequest>()))
      .thenReturn(CompletableFuture.completedFuture(subscriptionsByTopicResponse))
    whenever(snsClient.getSubscriptionAttributes(any<GetSubscriptionAttributesRequest>()))
      .thenReturn(CompletableFuture.completedFuture(subscriptionAttributesResponse))

    testConfig = AuthorisationConfigReader(fileManager).read("integration-test")
    policyManager = SubscriptionFilterPolicyManager(fileManager)
    updater = SubscriptionFilterPolicyUpdater(environment, testConfig, queueService, policyManager, telemetryService)
  }

  fun setExistingFilterPolicy(policy: FilterPolicy = FilterPolicy()) {
    policyManager.writePolicyValueAsString(policy)
    whenever(subscriptionAttributesResponse.attributes())
      .thenReturn(mapOf(SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME to policyManager.writePolicyValueAsString(policy)))
  }

  @Test
  fun `Should read the existing SNS filter policy, detect changes and write updated policy`() {
    setExistingFilterPolicy()
    whenever(subscriptionsByTopicResponse.subscriptions()).thenReturn(listOf(testQueueSubscription, anotherQueueSubscription))
    val attributesCaptor = argumentCaptor<SetSubscriptionAttributesRequest>()
    updater.init()
    verify(snsClient, times(1)).setSubscriptionAttributes(attributesCaptor.capture())
    val updatedAttributes = attributesCaptor.firstValue
    val updatedFilterPolicy = policyManager.readPolicyValueFromString(updatedAttributes.attributeValue())
    assertEquals(listOf("UPDATED_EVENT"), updatedFilterPolicy.eventType)
  }

  @Test
  fun `Should throw a Runtime exception when no SNS subscription filter policy is present for the queue`() {
    setExistingFilterPolicy()
    whenever(subscriptionsByTopicResponse.subscriptions()).thenReturn(listOf(anotherQueueSubscription))

    val exception =
      assertThrows<RuntimeException> {
        updater.init()
      }
    assertEquals("Failed to find an integration event subscription policy for testqueue. Please check the cloud platform configuration", exception.message)
  }
}
