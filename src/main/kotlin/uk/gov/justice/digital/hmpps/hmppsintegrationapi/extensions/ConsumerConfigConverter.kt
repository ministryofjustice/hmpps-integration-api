package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig

@Component
@ConfigurationPropertiesBinding
class ConsumerConfigConverter : Converter<String, ConsumerConfig> {
  override fun convert(source: String): ConsumerConfig? = ConsumerConfig(include = source.split("").map { it.trim() }.filter { it.isNotEmpty() })
}
