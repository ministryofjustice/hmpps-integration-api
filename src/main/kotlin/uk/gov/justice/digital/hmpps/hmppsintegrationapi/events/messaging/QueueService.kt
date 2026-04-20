package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.messaging

interface QueueService {
  fun sendMessageToQueue(
    rawMessage: String,
    queueName: String,
    eventType: String? = null,
  )
}
