package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.postgresql.util.PGobject
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.Filters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper

@Configuration
class JdbcConfig(
  private val filtersWritingConverter: FiltersWritingConverter,
  private val filtersReadingConverter: FiltersReadingConverter,
) : AbstractJdbcConfiguration() {
  override fun userConverters(): MutableList<*> =
    mutableListOf(
      filtersWritingConverter,
      filtersReadingConverter,
    )
}

@Component
@WritingConverter
class FiltersWritingConverter : Converter<Filters, PGobject> {
  override fun convert(source: Filters): PGobject {
    val jsonObject = PGobject()
    jsonObject.type = "jsonb"
    jsonObject.value = objectMapper.writeValueAsString(source)
    return jsonObject
  }
}

@Component
@ReadingConverter
class FiltersReadingConverter : Converter<PGobject, Filters> {
  override fun convert(pgObject: PGobject): Filters = objectMapper.readValue(pgObject.value, Filters::class.java)
}
