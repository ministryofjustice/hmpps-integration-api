package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.spy
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
import java.util.concurrent.ExecutionException
import kotlin.test.assertEquals

class SubscriptionFilterPolicyUpdaterTest {
  val fileManager = Mockito.spy(FileManager::class.java)!!
  val environment = mock(Environment::class.java)!!
  val topic = mock(HmppsTopic::class.java)!!
  val hmppsQueueService = mock(HmppsQueueService::class.java)!!

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
    whenever(topic.arn).thenReturn("event_topic_arn")
    whenever(topic.snsClient).thenReturn(snsClient)
    whenever(hmppsQueueService.findByTopicId(INTEGRATION_EVENT_TOPIC)).thenReturn(topic)
    whenever(hmppsQueueService.findByQueueId("testqueue")).thenReturn(testQueue)
    whenever(environment.activeProfiles).thenReturn(arrayOf("integration-test"))
    whenever(snsClient.listSubscriptionsByTopic(any<ListSubscriptionsByTopicRequest>()))
      .thenReturn(CompletableFuture.completedFuture(subscriptionsByTopicResponse))
    whenever(snsClient.getSubscriptionAttributes(any<GetSubscriptionAttributesRequest>()))
      .thenReturn(CompletableFuture.completedFuture(subscriptionAttributesResponse))

    testConfig = AuthorisationConfigReader(fileManager).read("integration-test")
    policyManager = SubscriptionFilterPolicyManager(fileManager)
    updater = SubscriptionFilterPolicyUpdater(environment, testConfig, hmppsQueueService, policyManager, telemetryService)
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

    await untilAsserted {
      verify(snsClient, times(1)).setSubscriptionAttributes(attributesCaptor.capture())
      val updatedAttributes = attributesCaptor.firstValue
      val updatedFilterPolicy = policyManager.readPolicyValueFromString(updatedAttributes.attributeValue())
      assertEquals(listOf("MAPPA_DETAIL_CHANGED"), updatedFilterPolicy.eventType)
    }
  }

  @Test
  fun `Should throw a Runtime exception when no SNS subscription filter policy is present for the queue`() {
    setExistingFilterPolicy()
    whenever(subscriptionsByTopicResponse.subscriptions()).thenReturn(listOf(anotherQueueSubscription))
    updater.init()
    val exception = argumentCaptor<RuntimeException>()
    await untilAsserted {
      verify(telemetryService).captureException(exception.capture())
      assertEquals("Failed to find an integration event subscription policy for testqueue. Please check the cloud platform configuration", exception.firstValue.message)
    }
  }

  @Test
  fun `Should throw a Runtime exception when no Filter Policy JSON file found for the consumer`() {
    setExistingFilterPolicy()
    val policyManagerSpy = spy(policyManager)
    updater = SubscriptionFilterPolicyUpdater(environment, testConfig, hmppsQueueService, policyManagerSpy, telemetryService)
    doAnswer { null }.whenever(policyManagerSpy).readPolicyFromClasspath(any(), any())
    updater.init()
    val exception = argumentCaptor<RuntimeException>()
    await untilAsserted {
      verify(telemetryService).captureException(exception.capture())
      assertEquals("Subscription filter policy for automated-test-client not found in the resources folder", exception.firstValue.message)
    }
  }

  @Test
  fun `Should log an error to sentry if the SNS update failed`() {
    setExistingFilterPolicy()
    whenever(subscriptionsByTopicResponse.subscriptions()).thenReturn(listOf(testQueueSubscription, anotherQueueSubscription))
    whenever(snsClient.setSubscriptionAttributes(any<SetSubscriptionAttributesRequest>()))
      .thenReturn(CompletableFuture.failedFuture(RuntimeException("SNS update error")))

    updater.init()
    val exception = argumentCaptor<ExecutionException>()
    await untilAsserted {
      verify(telemetryService).captureException(exception.capture())
      assertEquals("SNS update error", exception.firstValue.cause?.message)
    }
  }
}
