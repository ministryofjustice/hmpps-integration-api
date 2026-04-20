package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listener

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.AsyncAdapterBlockingExecutionFailedException
import io.awspring.cloud.sqs.listener.ListenerExecutionFailedException
import io.sentry.spring7.tracing.SentryTransaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.SQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.services.domain.DomainEventService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.util.concurrent.CompletionException

@ConditionalOnProperty("feature-flag.${FeatureFlagConfig.ENABLE_DOMAIN_EVENTS_QUEUE_LISTENER}", havingValue = "true")
@Service
@Transactional
class DomainEventsListener(
  @Autowired val domainEventService: DomainEventService,
  private val telemetryService: TelemetryService,
) {
  private companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  private val objectMapper = ObjectMapper()

  @SentryTransaction(operation = "messaging")
  @SqsListener("hmppsdomainqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onDomainEvent(rawMessage: String) {
    log.info("Received message: $rawMessage")
    try {
      val hmppsDomainEventMessage: SQSMessage = objectMapper.readValue(rawMessage)
      val hmppsDomainEvent: HmppsDomainEvent = objectMapper.readValue(hmppsDomainEventMessage.message)
      domainEventService.execute(hmppsDomainEvent)
    } catch (e: Exception) {
      telemetryService.captureException(unwrapSqsExceptions(e))
      throw e
    }
  }

  fun unwrapSqsExceptions(e: Throwable): Throwable {
    fun unwrap(e: Throwable) = e.cause ?: e
    var cause = e
    if (cause is CompletionException) {
      cause = unwrap(cause)
    }
    if (cause is AsyncAdapterBlockingExecutionFailedException) {
      cause = unwrap(cause)
    }
    if (cause is ListenerExecutionFailedException) {
      cause = unwrap(cause)
    }
    return cause
  }
}
