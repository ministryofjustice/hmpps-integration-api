package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.documentation.EventDocumentationManager
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.documentation.EndpointToGatewayDocumentationManager

object DocumentationGenerator {
  @JvmStatic
  fun main(args: Array<String>) {
    val generators =
      listOf(
        EventDocumentationManager(FileManager()),
        EndpointToGatewayDocumentationManager(FileManager()),
      )
    generators.forEach { it.generate() }
  }
}
