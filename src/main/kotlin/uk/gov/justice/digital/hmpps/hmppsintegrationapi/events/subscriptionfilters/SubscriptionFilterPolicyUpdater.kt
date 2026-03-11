package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_SUBSCRIPTION_FILTER_POLICY_UPDATER}", havingValue = "true")
@Component
class SubscriptionFilterPolicyUpdater(
  private val env: Environment,
  private val authorisationConfig: AuthorisationConfig,
  private val queueService: HmppsQueueService,
  private val subscriptionFilterPolicyManager: SubscriptionFilterPolicyManager,
) {
  private lateinit var hmppsEventsTopicSnsClient: SnsAsyncClient
  private lateinit var integrationEventTopicArn: String

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @PostConstruct
  fun init() {
    val hmppsEventTopic = queueService.findByTopicId(INTEGRATION_EVENT_TOPIC)
    integrationEventTopicArn = hmppsEventTopic!!.arn
    hmppsEventsTopicSnsClient = hmppsEventTopic.snsClient

    val environment = env.activeProfiles[0]
    authorisationConfig.consumersWithQueue().forEach { consumer ->
      val queueName = authorisationConfig.queueName(consumer)!!
      val filterPolicy = subscriptionFilterPolicyManager.readPolicyFromClasspath(environment, consumer)
      if (filterPolicy != null) {
        val awsSubscription = getSubscriptionFilterValue(queueName)
        val awsSubscriptionArn = awsSubscription.first
        val existingFilterPolicy = awsSubscription.second
        if (filterPolicy.eventType.toSet() != existingFilterPolicy.eventType.toSet()) {
          logger.info("Updating subscription filter policy for $consumer")
          setSubscriptionFilterValue(awsSubscriptionArn, subscriptionFilterPolicyManager.writePolicyValueAsString(filterPolicy))
        }
      }
    }
  }

  fun setSubscriptionFilterValue(
    subscriptionArn: String,
    value: String,
  ) {
    val request =
      SetSubscriptionAttributesRequest
        .builder()
        .subscriptionArn(subscriptionArn)
        .attributeName(SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME)
        .attributeValue(value)
        .build()
    hmppsEventsTopicSnsClient.setSubscriptionAttributes(request)
  }

  /**
   * Function to find the subscription filter policies for the integration event api that have been configured for a client queue.
   * Throws a runtime exception if no filter policy is found for the client queue
   *
   * @param clientQueueName
   * @return the arn and Filter Policy
   */

  fun getSubscriptionFilterValue(clientQueueName: String): Pair<String, FilterPolicy> {
    val queue = queueService.findByQueueId(clientQueueName) as HmppsQueue
    val listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder().topicArn(integrationEventTopicArn).build()
    val listSubscriptionsResponse = hmppsEventsTopicSnsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).get()

    return listSubscriptionsResponse
      .subscriptions()
      .firstOrNull {
        it.protocol() == "sqs" && it.endpoint() == queue.queueArn
      }?.let { sub ->
        val request = GetSubscriptionAttributesRequest.builder().subscriptionArn(sub.subscriptionArn()).build()
        val filterPolicy =
          hmppsEventsTopicSnsClient
            .getSubscriptionAttributes(request)
            .get()
            .attributes()[SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME]
            ?.let { subscriptionFilterPolicyManager.readPolicyValueFromString(it) }
            ?: FilterPolicy()

        sub.subscriptionArn() to filterPolicy
      } ?: throw RuntimeException("Failed to find an integration event subscription policy for $clientQueueName. Please check the cloud platform configuration")
  }
}
