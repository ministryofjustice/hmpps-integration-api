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
    try {
      hmppsIntegrationEventTopic = hmppsQueueService.findByTopicId(INTEGRATION_EVENT_TOPIC)!!
      val environment = env.activeProfiles[0]
      authorisationConfig.consumersWithQueue().forEach { consumer ->
        logger.info("Checking subscription filter policy for $consumer")
        val queueName = authorisationConfig.queueName(consumer)!!
        val filterPolicy = subscriptionFilterPolicyManager.readPolicyFromClasspath(environment, consumer)
        if (filterPolicy != null) {
          val subscriptionFilterValue = getSubscriptionFilterValue(queueName)
          val existingFilterPolicy = subscriptionFilterValue.filterPolicy
          if (filterPolicy.eventType.toSet() != existingFilterPolicy.eventType.toSet()) {
            logger.info("Updating subscription filter policy for $consumer")
            setSubscriptionFilterValue(
              subscriptionFilterValue.subscriptionArn,
              subscriptionFilterPolicyManager.writePolicyValueAsString(filterPolicy),
            )
          }
        }
      }
    } catch (ex: Exception) {
      telemetryService.captureException(ex)
      throw ex
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
    hmppsIntegrationEventTopic.snsClient.setSubscriptionAttributes(request)
  }

  /**
   * Function to find the subscription filter policies for the integration event api that have been configured for a consumer queue.
   * Throws a runtime exception if no filter policy is found for the consumer queue
   *
   * @param consumerQueueName
   * @return the arn and Filter Policy
   */

  fun getSubscriptionFilterValue(consumerQueueName: String): SubscriptionFilterPolicy {
    val consumerQueue = hmppsQueueService.findByQueueId(consumerQueueName) as HmppsQueue

    return getIntegrationEventSubscriptions()
      .firstOrNull {
        it.protocol() == "sqs" && it.endpoint() == consumerQueue.queueArn
      }?.let { subscription ->
        val request = GetSubscriptionAttributesRequest.builder().subscriptionArn(subscription.subscriptionArn()).build()
        val filterPolicy =
          hmppsIntegrationEventTopic.snsClient
            .getSubscriptionAttributes(request)
            .get()
            .attributes()[SUBSCRIPTION_FILTER_AWS_ATTRIBUTE_NAME]
            ?.let { subscriptionFilterPolicyManager.readPolicyValueFromString(it) }
            ?: FilterPolicy()

        SubscriptionFilterPolicy(filterPolicy, subscription.subscriptionArn())
      } ?: throw RuntimeException("Failed to find an integration event subscription policy for $consumerQueueName. Please check the cloud platform configuration")
  }

  /**
   * Gets all the subscriptions for the hmpps integration events topic
   *
   * @return a list of subscriptions
   */
  fun getIntegrationEventSubscriptions(): List<Subscription> {
    val listSubscriptionsByTopicRequest = ListSubscriptionsByTopicRequest.builder().topicArn(hmppsIntegrationEventTopic.arn).build()
    val listSubscriptionsResponse = hmppsIntegrationEventTopic.snsClient.listSubscriptionsByTopic(listSubscriptionsByTopicRequest).get()
    return listSubscriptionsResponse.subscriptions()
  }
}
