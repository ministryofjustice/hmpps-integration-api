package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.documentation

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.DocumentationManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.FileManager

class EventDocumentationManager(
  val fileManager: FileManager,
) : DocumentationManager {
  override fun generate() {
    val fileName = "index.html.md.erb"
    val path = fileManager.getDocumentationPath("events")
    val existingFileContent = fileManager.getDocumentationContents(path, fileName)

    val existing = extractEventSection(existingFileContent)
    val generated = generateEventsSection()
    if (existing != generated) {
      val updated = existingFileContent.replace(existing, generated)
      fileManager.write("$path/$fileName", updated)
    }
  }

  fun extractEventSection(existingFileContent: String): String = existingFileContent.substringAfter("<!---GENERATED-EVENTS-BEGIN--->\n").substringBefore("\n<!---GENERATED-EVENTS-END--->")

  fun generateEventsSection(): String {
    val content = StringBuilder()
    val numberOfEvents = IntegrationEventType.entries.size
    IntegrationEventType.entries.forEachIndexed { index, eventType ->

      val description = eventType.description
      val title = "### $description"
      val endpoint = "* **Endpoint:** `/${eventType.pathTemplate}`"
      val eventId = "* **Event ID:** `${eventType.name}`"
      content.appendLine(title)
      content.appendLine()
      content.appendLine(endpoint)
      content.appendLine(eventId)
      if (index < numberOfEvents - 1) {
        content.appendLine()
      }
    }
    return content.toString()
  }
}
