package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

class TestQueue(
  val id: String,
) : Queue {
  val messages = mutableListOf<String>()

  override fun queueId() = id

  override fun queueName() = "Queue $id"

  override fun sendMessage(
    eventType: String,
    event: String,
  ) {
    messages.add(event)
  }

  override fun messageCount() = messages.size

  override fun lastMessage() = messages.lastOrNull()

  fun clearMessages() {
    messages.clear()
  }

}
