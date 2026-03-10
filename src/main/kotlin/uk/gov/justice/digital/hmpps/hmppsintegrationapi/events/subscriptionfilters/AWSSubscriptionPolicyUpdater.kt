package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.subscriptionfilters

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.model.GetSubscriptionAttributesRequest
import software.amazon.awssdk.services.sns.model.ListSubscriptionsByTopicRequest
import software.amazon.awssdk.services.sns.model.SetSubscriptionAttributesRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Component
class AWSSubscriptionPolicyUpdater(
  private val env: Environment,
  private val authorisationConfig: AuthorisationConfig,
  private val queueService: HmppsQueueService,
) {
  private lateinit var hmppsEventsTopicSnsClient: SnsAsyncClient
  private lateinit var integrationEventTopicArn: String

  val jsonMapper = ObjectMapper().registerKotlinModule()

  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @PostConstruct
  fun init() {
    val hmppsEventTopic = queueService.findByTopicId("integrationeventtopic")
    integrationEventTopicArn = hmppsEventTopic!!.arn
    hmppsEventsTopicSnsClient = hmppsEventTopic.snsClient

    val environment = env.activeProfiles[0]
    authorisationConfig.consumersWithQueue().forEach { consumer ->
      val queueName = authorisationConfig.queueName(consumer)!!
      val policyFile = ClassPathResource("$SUBSCRIPTION_FILTER_FOLDER_NAME/$environment/$consumer-$SUBSCRIPTION_FILTER_FILE_SUFFIX")
      if (policyFile.exists()) {
        val filterPolicy = jsonMapper.readValue(policyFile.file, FilterPolicy::class.java)
        val awsSubscription = getSubscriptionFilterValue(queueName)
        val awsSubscriptionArn = awsSubscription.first
        val existingFilterPolicy = awsSubscription.second
        if (filterPolicy.eventType.toSet() != existingFilterPolicy.eventType.toSet()) {
          logger.info("Updating subscription filter policy for $consumer")
          setSubscriptionFilterValue(awsSubscriptionArn, policyFile.file.readText())
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
   * Throws a runtime exception if no
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
            ?.let { jsonMapper.readValue(it, FilterPolicy::class.java) }
            ?: FilterPolicy()

        sub.subscriptionArn() to filterPolicy
      } ?: throw RuntimeException("Failed to find an integration event subscription policy for $clientQueueName. Please check the cloud platform configuration")
  }
}
