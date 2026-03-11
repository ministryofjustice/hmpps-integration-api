package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.Subscription
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters.FilterPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters.SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters.SubscriptionFilterPolicyUpdater
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic

class SubscriptionFilterPolicyUpdaterIntegrationTest : IntegrationTestBase() {
  @Autowired
  private lateinit var queueService: HmppsQueueService

  @Autowired
  private lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  private lateinit var subscriptionFilterPolicyUpdater: SubscriptionFilterPolicyUpdater

  private lateinit var client: SnsAsyncClient

  private lateinit var subscriptionArn: String

  @BeforeEach
  fun setup() {
    val topic = queueService.findByTopicId(INTEGRATION_EVENT_TOPIC) as HmppsTopic
    client = topic.snsClient
    subscriptionArn = getTestSubscription()?.subscriptionArn()!!
    val request =
      SetSubscriptionAttributesRequest
        .builder()
        .subscriptionArn(subscriptionArn)
        .attributeName(SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME)
        .attributeValue("{\"eventType\":[\"EXISTING_EVENT\"]}")
        .build()
    client.setSubscriptionAttributes(request)
  }

  fun getTestSubscription(): Subscription? {
    val topic = queueService.findByTopicId("integrationeventtopic") as HmppsTopic
    val queue = queueService.findByQueueId("testqueue") as HmppsQueue
    val listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder().topicArn(topic.arn).build()
    val listSubscriptionsResponse = client.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).get()
    return listSubscriptionsResponse.subscriptions().firstOrNull {
      it.protocol() == "sqs" && it.endpoint() == queue.queueArn
    }
  }

  fun getTestFilterPolicy(): FilterPolicy? {
    val request = GetSubscriptionAttributesRequest.builder().subscriptionArn(subscriptionArn).build()
    return client
      .getSubscriptionAttributes(request)
      .get()
      .attributes()[SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME]
      ?.let { ObjectMapper().registerKotlinModule().readValue(it, FilterPolicy::class.java) }
  }

  @Test
  fun `Policy updater should update the subscription policy filter for the integration-test consumer on application start`() {
    subscriptionFilterPolicyUpdater.init()
    val updatedTestPolicy = getTestFilterPolicy()
    assertThat(updatedTestPolicy).isEqualTo(FilterPolicy(listOf("UPDATED_EVENT")))
  }
}
