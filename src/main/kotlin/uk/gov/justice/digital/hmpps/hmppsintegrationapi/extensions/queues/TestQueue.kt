package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues

class TestQueue(val id: String) : Queue {
  val messages = mutableListOf<String>()
  override fun getId() = id

  override fun getName() = "Queue $id"

  override fun sendMessage(eventType: String, event: String) {
    messages.add(event)
  }

  fun messageCount() = messages.size

  fun clearMessages() {
    messages.clear()
  }

  fun lastMessage() = messages.lastOrNull()
}
