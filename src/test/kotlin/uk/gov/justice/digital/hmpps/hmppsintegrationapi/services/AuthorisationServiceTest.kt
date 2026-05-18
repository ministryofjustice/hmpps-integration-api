package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AuthorisationServiceTest : ConfigTest() {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
  }

  val testRole =
    role("test-role") {
      permissions {
        -"/persons/123"
      }
    }

  @Test
  fun `has permission`() {
    assertTrue(getAuthService("dev").hasAccess("smoke-test-full-access", "/v1/status"))
  }

  @Test
  fun `has permission to specific path`() {
    assertTrue(getAuthService("dev").hasAccess("smoke-test-full-access", "/v1/persons/ABC123"))
  }

  @Test
  fun `does not have permission`() {
    assertFalse(getAuthService("dev").hasAccess("smoke-test-limited-access", "/v9/status"))
  }

  @Test
  fun `multiple matching consumers`() {
    for (env in listOf("dev", "preprod", "prod")) {
      val matches = getAuthService(env).consumersWithAccess("/v1/status")
      assertNotEquals(0, matches.size)
      assertContains(matches, "event-service")
      assertContains(matches, "kubernetes-health-check-client")
    }
  }

  fun listConsumersWithAccess(
    environment: String,
    endpoint: String,
  ): List<String> {
    val matches = getAuthService(environment).consumersWithAccess(endpoint)
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
    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "c1" to ConsumerConfig(include = listOf("/tester"), filters = null, roles = listOf()),
            "c2" to ConsumerConfig(include = listOf("/tester", "/other"), filters = null, roles = listOf()),
            "c3" to ConsumerConfig(include = listOf("/other"), filters = null, roles = listOf()),
          ),
        ),
      )

    val matches = authorisationService.consumersWithAccess("/tester")

    assertEquals(2, matches.size)
    assertContains(matches, "c1")
    assertContains(matches, "c2")
  }

  @Test
  fun `compare missing and empty lists in ConsumerConfig`() {
    val missingConfig =
      parseAuthorisationService(
        """
        consumers:
          tester:
            roles:
              - full-access
        """.trimIndent(),
      )

    val emptyConfig =
      parseAuthorisationService(
        """
        consumers:
          tester:
            include:
            filters:
            roles:
              - full-access
        """.trimIndent(),
      )

    assertEquals(missingConfig.consumers()["tester"]?.permissions(), emptyConfig.consumers()["tester"]?.permissions())
    assertEquals(missingConfig.consumers()["tester"]?.filters, emptyConfig.consumers()["tester"]?.filters)
  }

  @Test
  fun `no supervision status filters in role`() {
    val consumer = ConsumerConfig(roles = listOf("testing"))
    val role = role("testing") {}

    val filters =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                include = null,
                filters = ConsumerFilters(prisons = listOf("MDI")),
                roles = listOf("test-role"),
              ),
          ),
          roles = mapOf("test-role" to testRole),
        ),
      ).allFilters(consumer, listOf(role))

    assertEquals(filters, ConsumerFilters.Companion.NO_FILTERS)
  }

  @Test
  fun `prison supervision status filter ONLY in role`() {
    val consumer = ConsumerConfig(roles = listOf("testing"))

    val role =
      role("testing") {
        filters {
          supervisionStatuses {
            -"PRISONS"
          }
        }
      }

    val filters =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                include = null,
                filters = ConsumerFilters(prisons = listOf("MDI")),
                roles = listOf("test-role"),
              ),
          ),
          roles = mapOf("test-role" to testRole),
        ),
      ).allFilters(consumer, listOf(role))

    assertFalse(filters == ConsumerFilters.Companion.NO_FILTERS)
    assertTrue(filters.hasSupervisionStatusesFilter())
    assertTrue(filters.isPrisonsOnly())
    assertFalse(filters.isProbationOnly())
  }

  @Test
  fun `probation supervision status filter ONLY in role`() {
    val consumer = ConsumerConfig(roles = listOf("testing"))

    val role =
      role("testing") {
        filters {
          supervisionStatuses {
            -"PROBATION"
          }
        }
      }

    val filters =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                include = null,
                filters = ConsumerFilters(prisons = listOf("MDI")),
                roles = listOf("test-role"),
              ),
          ),
          roles = mapOf("test-role" to testRole),
        ),
      ).allFilters(consumer, listOf(role))

    assertFalse(filters == ConsumerFilters.Companion.NO_FILTERS)
    assertTrue(filters.hasSupervisionStatusesFilter())
    assertTrue(filters.isProbationOnly())
    assertFalse(filters.isPrisonsOnly())
  }

  @Test
  fun `probation and prisons supervision statuses filter exists in role`() {
    val consumer = ConsumerConfig(roles = listOf("testing"))

    val role =
      role("testing") {
        filters {
          supervisionStatuses {
            -"PROBATION"
            -"PRISONS"
          }
        }
      }

    val filters =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                include = null,
                filters = ConsumerFilters(prisons = listOf("MDI")),
                roles = listOf("test-role"),
              ),
          ),
          roles = mapOf("test-role" to testRole),
        ),
      ).allFilters(consumer, listOf(role))

    assertFalse(filters == ConsumerFilters.Companion.NO_FILTERS)
    assertTrue(filters.hasSupervisionStatusesFilter())
    assertFalse(filters.isProbationOnly())
    assertFalse(filters.isPrisonsOnly())
    assertTrue(filters.hasPrisons())
    assertTrue(filters.hasProbation())
  }
}
