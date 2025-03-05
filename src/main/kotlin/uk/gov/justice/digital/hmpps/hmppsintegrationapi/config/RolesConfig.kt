package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.Role
import java.io.IOException

@Configuration
@PropertySource(value = ["classpath:/roles.yml"], factory = MultipleYamlPropertySourceFactory::class)
class RolesConfig(
  val roles: List<Role>,
)

class MultipleYamlPropertySourceFactory : PropertySourceFactory {
  @Throws(IOException::class)
  override fun createPropertySource(
    name: String,
    encodedResource: EncodedResource,
  ): org.springframework.core.env.PropertySource<*> {
    val factory = YamlPropertiesFactoryBean()
    factory.setResources(encodedResource.resource)

    val properties = factory.getObject()

    return PropertiesPropertySource(encodedResource.resource.filename, properties)
  }
}
