@file:Suppress("ktlint:standard:no-wildcard-imports")

package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.io.IOException
import java.util.*

@Configuration
class YamlPropertySourceFactory : PropertySourceFactory {
  override fun createPropertySource(
    name: String?,
    resource: EncodedResource,
  ): PropertySource<*> =
    try {
      val factory = YamlPropertiesFactoryBean()
      factory.setResources(resource.resource)
      val properties = factory.getObject() ?: Properties()
      PropertiesPropertySource(resource.resource.filename ?: "yaml-resource", properties)
    } catch (e: IOException) {
      println("Error loading YAML: ${e.message}")
      PropertiesPropertySource("empty", Properties())
    }
}
