package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Component
@ConfigurationPropertiesBinding
class ConsumerConfigConverter : Converter<String, ConsumerConfig> {
  // Specifically used in the case where there is a consumer config with no fields
  override fun convert(source: String): ConsumerConfig? = ConsumerConfig(include = emptyList(), filters = ConsumerFilters(prisons = null))
}
