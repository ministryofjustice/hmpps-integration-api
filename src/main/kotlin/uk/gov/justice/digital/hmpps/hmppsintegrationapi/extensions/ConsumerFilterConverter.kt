package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Component
@ConfigurationPropertiesBinding
class ConsumerFilterConverter : Converter<String, ConsumerFilters> {
  override fun convert(source: String): ConsumerFilters? = ConsumerFilters()
}
