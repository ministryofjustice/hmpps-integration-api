package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.documentation.EventDocumentationManager

object DocumentationGenerator {
  @JvmStatic
  fun main(args: Array<String>) {
    val generators =
      listOf(
        EventDocumentationManager(FileManager()),
        // Temp removal until stable
        // EndpointToGatewayDocumentationManager(FileManager()),
      )
    generators.forEach { it.generate() }
  }
}
