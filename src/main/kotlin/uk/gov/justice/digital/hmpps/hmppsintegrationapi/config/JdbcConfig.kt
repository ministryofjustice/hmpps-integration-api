package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Metadata
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper

@Configuration
class JdbcConfig(
  private val metadataWritingConverter: MetadataWritingConverter,
  private val metadataReadingConverter: MetadataReadingConverter,
) : AbstractJdbcConfiguration() {
  override fun userConverters(): MutableList<*> =
    mutableListOf(
      metadataWritingConverter,
      metadataReadingConverter,
    )
}

@Component
@WritingConverter
class MetadataWritingConverter : Converter<Metadata, String> {
  override fun convert(source: Metadata): String = objectMapper.writeValueAsString(source)
}

@Component
@ReadingConverter
class MetadataReadingConverter : Converter<String, Metadata> {
  override fun convert(source: String): Metadata = objectMapper.readValue(source, Metadata::class.java)
}
