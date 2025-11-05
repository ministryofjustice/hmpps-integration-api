package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthorisationConfigTest : ConfigTest() {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  @Test
  fun `has permission`() {
    assertTrue(getAuthConfig("dev").hasAccess("smoke-test-full-access", "/v1/status"))
  }

  @Test
  fun `has permission to generic path`() {
    assertTrue(getAuthConfig("dev").hasAccess("smoke-test-full-access", "/v1/persons/{hmppsId}"))
  }

  @Test
  fun `has permission to specific path`() {
    assertTrue(getAuthConfig("dev").hasAccess("smoke-test-full-access", "/v1/persons/ABC123"))
  }

  @Test
  fun `does not have permission`() {
    assertFalse(getAuthConfig("dev").hasAccess("smoke-test-limited-access", "/v9/status"))
  }

  @Test
  fun `multiple matching consumers`() {
    for (env in listOf("dev", "preprod", "prod")) {
      val matches = getAuthConfig(env).consumersWithAccess("/v1/status")
      assertNotEquals(0, matches.size)
      assertContains(matches, "event-service")
      assertContains(matches, "kubernetes-health-check-client")
    }
  }

  fun listConsumersWithAccess(
    environment: String,
    endpoint: String,
  ): List<String> {
    val matches = getAuthConfig(environment).consumersWithAccess(endpoint)
    logger.info("Consumers with access to {} in {} : {}", endpoint, environment, matches)
    return matches
  }

  @Test
  fun `show permission matches`() {
    val endpoint = "/health/readiness"
    val environment = "preprod"

    // You can temporarily change endpoint & environment to see who has access to what, where
    val matches = listConsumersWithAccess(environment, endpoint)

    assertEquals(3, matches.size)
    assertContains(matches, "kubernetes-health-check-client")
  }

  @Test
  fun `validate core endpoint matching with synthetic data`() {
    val authConfig = AuthorisationConfig()
    authConfig.consumers =
      mapOf(
        "c1" to ConsumerConfig(include = listOf("/tester"), filters = null, roles = listOf()),
        "c2" to ConsumerConfig(include = listOf("/tester", "/other"), filters = null, roles = listOf()),
        "c3" to ConsumerConfig(include = listOf("/other"), filters = null, roles = listOf()),
      )

    val matches = authConfig.consumersWithAccess("/tester")

    assertEquals(2, matches.size)
    assertContains(matches, "c1")
    assertContains(matches, "c2")
  }

  @Test
  fun `compare missing and empty lists in ConsumerConfig`() {
    val missingConfig =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )

    val emptyConfig =
      parseConfig<AuthorisationConfig>(
        """
        consumers:
          tester:
            include:
            filters:
            roles:
              - full-access
        """.trimIndent(),
      )

    assertEquals(missingConfig.consumers["tester"]?.permissions(), emptyConfig.consumers["tester"]?.permissions())
    assertEquals(missingConfig.consumers["tester"]?.filters, emptyConfig.consumers["tester"]?.filters)
  }
}
