package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.junit.jupiter.api.Order
import org.springframework.boot.context.TypeExcludeFilter
import org.springframework.boot.test.context.TestComponent
import org.springframework.core.type.classreading.MetadataReader
import org.springframework.core.type.classreading.MetadataReaderFactory

/**
 * This is used by the integration test base to exclude scanning of the events package
 * This prevents any database related components being required in the API integration tests
 */
@TestComponent
@Order(1)
class EventsExcludeFilter : TypeExcludeFilter() {
  override fun match(
    metadataReader: MetadataReader,
    metadataReaderFactory: MetadataReaderFactory,
  ): Boolean {
    return metadataReader.classMetadata.className.contains("hmppsintegrationapi.events") // Exclude this service
  }
}
