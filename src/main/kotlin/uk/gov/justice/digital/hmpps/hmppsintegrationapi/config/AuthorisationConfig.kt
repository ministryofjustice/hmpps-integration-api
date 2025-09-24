package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import java.io.FileNotFoundException

@Configuration
@Component
@ConfigurationProperties(prefix = "authorisation")
class AuthorisationConfig {
  var consumers: Map<String, ConsumerConfig?> = emptyMap()
}

/**
 * Utility to determine whether a consumer has permissions for an endpoint in a particular environment.
 */
class PermissionChecker(
  val authProvider: AuthorisationConfigProvider = DefaultAuthorisationConfigProvider(),
) {
  /**
   * Returns true if the user has access to an endpoint in an environment.
   */
  fun hasPermission(
    endpoint: String,
    environment: String,
    username: String,
  ): Boolean = consumersWithPermission(endpoint, environment).contains(username)

  /**
   * Returns a sorted list of all users with access to an endpoint in an environment.
   */
  fun consumersWithPermission(
    endpoint: String,
    environment: String,
  ): List<String> {
    val matches = mutableSetOf<String>()

    for ((name, config) in authProvider.getConfig(environment).consumers) {
      if (hasAccess(config, endpoint)) {
        matches.add(name)
      }
    }

    return matches.toList().sorted()
  }

  private fun hasAccess(
    config: ConsumerConfig?,
    endpoint: String,
  ): Boolean {
    if (config?.include?.contains(endpoint) == true) return true
    for (roleName in config?.roles ?: emptyList()) {
      if (roleCanAccess(roleName, endpoint)) return true
    }
    return false
  }

  private fun roleCanAccess(
    roleName: String,
    endpoint: String,
  ): Boolean = roles[roleName]?.include?.contains(endpoint) == true
}

/**
 * Provides access to the consumer configuration in an environment.
 */
interface AuthorisationConfigProvider {
  fun getConfig(environment: String): AuthorisationConfig
}

class DefaultAuthorisationConfigProvider : AuthorisationConfigProvider {
  override fun getConfig(environment: String): AuthorisationConfig {
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    try {
      val consumers =
        mapper
          .readTree(ClassPathResource("application-$environment.yml").file)
          .path("authorisation")
          .path("consumers")
      val authConfig = AuthorisationConfig()
      authConfig.consumers = mapper.convertValue(consumers, object : TypeReference<Map<String, ConsumerConfig?>>() {})
      return authConfig
    } catch (e: FileNotFoundException) {
      return AuthorisationConfig()
    }
  }
}
