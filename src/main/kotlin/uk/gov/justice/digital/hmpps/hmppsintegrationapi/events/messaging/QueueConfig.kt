package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.QueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging.sqs.AwsQueueService
import uk.gov.justice.hmpps.sqs.HmppsQueueService

@Configuration
class QueueConfig {
  @Bean
  fun queueService(hmppsQueueService: HmppsQueueService): QueueService = AwsQueueService(hmppsQueueService)
}
