package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import java.util.*


@Configuration
@Component
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationConfig {
  var consumers: Map<String, ConsumerConfig?> = emptyMap()
}


/**
 * Utility to determine whether a consumer has permissions for an endpoint in a particular environment.
 */
class PermissionChecker(val environmentPropertyProvider: EnvironmentPropertyProvider = DefaultEnvironmentPropertyProvider()) {

  /**
   * Returns true if the user has access to an endpoint in an environment.
   */
  fun hasPermission(endpoint: String, environment: String, username: String): Boolean {
    return consumersWithPermission(endpoint, environment).contains(username)
  }

  /**
   * Returns a sorted list of all users with access to an endpoint in an environment.
   */
  fun consumersWithPermission(endpoint: String, environment: String): List<String> {
    val matches = mutableSetOf<String>()

    for ((key, value) in environmentPropertyProvider.getConfig(environment)) {
      if (grantsAccess(key, value, endpoint)) {
        matches.add(propertyConsumerName(key))
      }
    }

    return matches.toList().sorted()
  }

  private fun propertyConsumerName(key: String): String {
    val keyParts = key.split(".")
    return if (keyParts.size >= 3) keyParts[2] else "{unknown}"
  }

  private fun grantsAccess(key: String, value: String, endpoint: String): Boolean =
    isDirectAccess(key, value, endpoint) ||
    isRoleAccess(key, value, endpoint)

  private fun isRoleAccess(key: String, value: String, endpoint: String): Boolean =
    isRoleDef(key) && roleCanAccess(value, endpoint)

  private fun isDirectAccess(key: String, value: String, endpoint: String): Boolean =
    isInclude(key) && value.equals(endpoint)

  private fun isInclude(key: String): Boolean =
    key.contains(".include[")

  private fun isRoleDef(key: String): Boolean =
    key.contains(".roles[")

  private fun roleCanAccess(roleName: String, endpoint: String): Boolean =
    roles[roleName]?.include?.contains(endpoint) == true
}

/**
 * Provides access to the properties of an environment (as strings).
 */
interface EnvironmentPropertyProvider {
  fun getConfig(environment: String): Map<String,String>
}

class DefaultEnvironmentPropertyProvider: EnvironmentPropertyProvider {
  override fun getConfig(environment: String): Map<String,String> {
    val yaml = YamlPropertiesFactoryBean()
    yaml.setResources(ClassPathResource("application-$environment.yml"))
    return stringProperties(yaml.getObject()!!)
  }

  private fun stringProperties(properties: Properties): Map<String, String> =
    properties.map { it.key.toString() to it.value.toString() }.toMap()
}
