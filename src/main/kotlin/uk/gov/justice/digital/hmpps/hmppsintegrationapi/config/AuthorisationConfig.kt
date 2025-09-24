package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.LoggerFactory
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

  fun hasAccess(
    consumerName: String,
    endpoint: String,
  ): Boolean {
    val config = consumers[consumerName]
    if (anyMatch(config?.include, endpoint)) return true
    for (roleName in config?.roles ?: emptyList()) {
      if (roleCanAccess(roleName, endpoint)) return true
    }
    return false
  }

  fun consumersWithAccess(endpoint: String): List<String> =
    consumers
      .filter { hasAccess(it.key, endpoint) }
      .map { it.key }
      .toList()
      .sorted()

  private fun anyMatch(
    patterns: List<String>?,
    endpoint: String,
  ): Boolean = patterns != null && patterns.any { Regex(it).matches(endpoint) }

  private fun roleCanAccess(
    roleName: String,
    endpoint: String,
  ): Boolean = anyMatch(roles[roleName]?.include, endpoint)
}

/**
 * Utility to determine whether a consumer has permissions for an endpoint in a particular environment.
 */
class PermissionChecker(
  val authProvider: AuthorisationConfigProvider = AuthorisationConfigProvider(),
) {
  /**
   * Returns true if the user has access to an endpoint in an environment.
   */
  fun hasPermission(
    endpoint: String,
    environment: String,
    username: String,
  ): Boolean = authProvider.getConfig(environment).hasAccess(username, endpoint)

  /**
   * Returns a sorted list of all users with access to an endpoint in an environment.
   */
  fun consumersWithPermission(
    endpoint: String,
    environment: String,
  ): List<String> =
    authProvider
      .getConfig(environment)
      .consumersWithAccess(endpoint)
}

/**
 * Provides access to the consumer configuration in an environment.
 */
class AuthorisationConfigProvider {
  val log = LoggerFactory.getLogger(this.javaClass)

  fun getConfig(environment: String): AuthorisationConfig {
    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
    try {
      val authConfig =
        mapper
          .readTree(ClassPathResource("application-$environment.yml").file)
          .path("authorisation")
      return mapper.convertValue(authConfig, object : TypeReference<AuthorisationConfig>() {})
    } catch (e: FileNotFoundException) {
      log.warn("No authorisation configuration found for environment: $environment")
      return AuthorisationConfig()
    }
  }
}
