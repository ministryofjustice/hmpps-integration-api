package uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Component
@ConfigurationPropertiesBinding
class RoleFilterConverter : Converter<String, RoleFilters> {
  override fun convert(source: String): RoleFilters? = RoleFilters(prisons = null, caseNotes = null)
}
