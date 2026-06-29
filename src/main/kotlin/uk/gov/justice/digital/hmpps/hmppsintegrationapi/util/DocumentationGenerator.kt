package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.documentation.EventDocumentationManager

object DocumentationGenerator {
  @JvmStatic
  fun main(args: Array<String>) {
    val generators =
      listOf<DocumentationManager>(
        EventDocumentationManager(FileManager()),
      )
    generators.forEach { it.generate() }
  }
}
