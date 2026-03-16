package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.Subscription
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.INTEGRATION_EVENT_TOPIC
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.HmppsTopic

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_SUBSCRIPTION_FILTER_POLICY_UPDATER}", havingValue = "true")
@Component
class SubscriptionFilterPolicyUpdater(
  private val env: Environment,
  private val authorisationConfig: AuthorisationConfig,
  private val hmppsQueueService: HmppsQueueService,
  private val subscriptionFilterPolicyManager: SubscriptionFilterPolicyManager,
  private val telemetryService: TelemetryService,
) {
  private lateinit var hmppsIntegrationEventTopic: HmppsTopic

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @PostConstruct
  fun init() {
    hmppsIntegrationEventTopic = hmppsQueueService.findByTopicId(INTEGRATION_EVENT_TOPIC)!!
    Thread { execute() }.start()
  }

  /**
   * Checks and updates where required, the subscription policy filters for all consumers
   * with a queue in the current environment
   *
   */
  fun execute() {
    val environment = env.activeProfiles[0]
    authorisationConfig.consumersWithQueue().forEach { consumer ->
      try {
        checkSubscriptionFilter(environment, consumer)
      } catch (ex: Exception) {
        telemetryService.captureException(ex)
      }
    }
  }

  /**
   * Checks and updates where required, the subscription policy filters for
   * a consumer in the current environment
   *
   * @param environment The environment for which to perform the update
   * @param consumer The consumer
   */
  fun checkSubscriptionFilter(
    environment: String,
    consumer: String,
  ) {
    logger.info("Checking subscription filter policy for $consumer")
    val queueName = authorisationConfig.queueName(consumer)!!
    val filterPolicy =
      subscriptionFilterPolicyManager.readPolicyFromClasspath(environment, consumer)
        ?: throw RuntimeException("Subscription filter policy for $consumer not found in the resources folder")
    val subscriptionFilterValue = getSubscriptionFilterValue(queueName)
    val existingFilterPolicy = subscriptionFilterValue.filterPolicy
    if (policyObject(filterPolicy) != policyObject(existingFilterPolicy)) {
      logger.info("Updating subscription filter policy for $consumer")
      setSubscriptionFilterValue(
        consumer,
        subscriptionFilterValue.subscriptionArn,
        filterPolicy,
      )
    } else {
      logger.info("No changes have been identified in the subscription filter policy for $consumer")
    }
  }

  /**
   * Read JSON string into a policy object for comparison
   *
   * @param policyJsonString the policy JSON string
   * @return a FilterPolicy or null if policyJsonString is null
   */
  fun policyObject(policyJsonString: String?): FilterPolicy? = policyJsonString?.let { subscriptionFilterPolicyManager.readPolicyValueFromString(it) }

  /**
   * Sets the FilterPolicy attribute for the subscription arn
   *
   * @param subscriptionArn
   * @param value
   */
  fun setSubscriptionFilterValue(
    consumer: String,
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

    val response =
      hmppsIntegrationEventTopic.snsClient
        .setSubscriptionAttributes(request)
        .get()
        .sdkHttpResponse()
    if (response.isSuccessful) {
      logger.info("Subscription filter policy for $consumer successfully updated")
    } else {
      throw RuntimeException("Attempt to set subscription filter policy for $consumer failed with status ${response.statusCode()}")
    }
  }

  /**
   * Function to find the subscription filter policies for the integration event api that have been configured for a consumer queue.
   * Throws a runtime exception if no filter policy is found for the consumer queue
   *
   * @param consumerQueueName
   * @return the arn and Filter Policy value
   */

  fun getSubscriptionFilterValue(consumerQueueName: String): SubscriptionFilterPolicy {
    val consumerQueue = hmppsQueueService.findByQueueId(consumerQueueName) as HmppsQueue
    val subscription = getIntegrationEventSubscriptionForQueue(consumerQueueName, consumerQueue.queueArn!!)
    val request = GetSubscriptionAttributesRequest.builder().subscriptionArn(subscription.subscriptionArn()).build()
    val filterPolicy =
      hmppsIntegrationEventTopic.snsClient
        .getSubscriptionAttributes(request)
        .get()
        .attributes()[SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME]
    return SubscriptionFilterPolicy(filterPolicy, subscription.subscriptionArn())
  }

  /**
   * Gets the subscription to the hmpps integration events topic for the consumers queue
   *
   * @return the subscription
   */
  fun getIntegrationEventSubscriptionForQueue(
    queueName: String,
    queueArn: String,
  ): Subscription {
    val listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder().topicArn(hmppsIntegrationEventTopic.arn).build()
    val listSubscriptionsResponse = hmppsIntegrationEventTopic.snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).get()
    return listSubscriptionsResponse.subscriptions().firstOrNull {
      it.protocol() == "sqs" && it.endpoint() == queueArn
    } ?: throw RuntimeException("Failed to find an integration event subscription policy for $queueName. Please check the cloud platform configuration")
  }
}
