package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.provider.Arguments
import org.mockito.kotlin.times
import org.mockito.kotlin.whenever
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig.OboConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.policies.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.JwksOboService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.UnsignedJwtOboService
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthorisationServiceTest : ConfigTest() {
  companion object {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    fun expiryBandTestArgs() =
      listOf(
        Arguments.of(listOf<Long>(0, 1, 2, 3, 4, 5, 6, 7), 8),
        Arguments.of(listOf<Long>(8, 9, 10, 11, 12, 13, 14), 1),
        Arguments.of(listOf<Long>(15, 16, 17, 18, 19, 20, 21), 1),
        Arguments.of(listOf<Long>(22, 23, 24, 25, 26, 27, 28), 1),
        Arguments.of(listOf<Long>(29, 30), 1),
      )
  }

  val testRole =
    role("test-role") {
      permissions {
        -"/persons/123"
      }
      redactionPolicies {
        -laoRedactionPolicy
      }
    }

  @Test
  fun `has permission`() {
    assertTrue(getAuthService("test").hasAccess("automated-test-client", "/v1/status"))
  }

  @Test
  fun `has permission to specific path`() {
    assertTrue(getAuthService("test").hasAccess("automated-test-client", "/v1/persons/ABC123"))
  }

  @Test
  fun `does not have permission`() {
    assertFalse(getAuthService("test").hasAccess("automated-test-client", "/v9/status"))
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
    val endpoint = "/v1/persons"
    val environment = "test"

    // You can temporarily change endpoint & environment to see who has access to what, where
    val matches = listConsumersWithAccess(environment, endpoint)
    assertContains(matches, "automated-test-client")
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
        mockTelemetryService,
        mockManageUsersService,
      )

    val matches = authorisationService.consumersWithAccess("/tester")

    assertEquals(2, matches.size)
    assertContains(matches, "c1")
    assertContains(matches, "c2")
  }

  @Test
  fun `compare missing and empty lists in ConsumerConfig`() {
    val missingConfig =
      AuthorisationService(
        parseAuthorisationConfig(
          """
          consumers:
            tester:
              roles:
                - full-access
          """.trimIndent(),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )

    val emptyConfig =
      AuthorisationService(
        parseAuthorisationConfig(
          """
          consumers:
            tester:
              include:
              filters:
              roles:
                - full-access
          """.trimIndent(),
        ),
        mockTelemetryService,
        mockManageUsersService,
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
        mockTelemetryService,
        mockManageUsersService,
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
        mockTelemetryService,
        mockManageUsersService,
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
        mockTelemetryService,
        mockManageUsersService,
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
        mockTelemetryService,
        mockManageUsersService,
      ).allFilters(consumer, listOf(role))

    assertFalse(filters == ConsumerFilters.Companion.NO_FILTERS)
    assertTrue(filters.hasSupervisionStatusesFilter())
    assertFalse(filters.isProbationOnly())
    assertFalse(filters.isPrisonsOnly())
    assertTrue(filters.hasPrisons())
    assertTrue(filters.hasProbation())
  }

  @Test
  fun `returns redaction policies`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                roles = listOf("test-role"),
              ),
          ),
          roles = mapOf("test-role" to testRole),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(listOf(laoRedactionPolicy), service.redactionPolicies("consumer-name"))
  }

  @Test
  fun `returns null for no oboConfig`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(null, service.oboService("consumer-name"))
  }

  @Test
  fun `returns UnsignedJwtOboService for unsigned oboConfig`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                oboConfig = OboConfig("unsigned"),
              ),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(UnsignedJwtOboService()::class::java, service.oboService("consumer-name")!!::class::java)
  }

  @Test
  fun `returns valid entra oboConfig`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                oboConfig = OboConfig("entra"),
              ),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(JwksOboService::class, service.oboService("consumer-name")!!::class)
  }

  @Test
  fun `returns true if oboConfig has a value and required is set to true`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                oboConfig = OboConfig("entra"),
              ),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(true, service.requiresObo("consumer-name"))
  }

  @Test
  fun `returns false if oboConfig has a value and required is set to false`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(
                oboConfig = OboConfig("entra", required = false),
              ),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(false, service.requiresObo("consumer-name"))
  }

  @Test
  fun `returns false if oboConfig has no value`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(false, service.requiresObo("consumer-name"))
  }

  @Test
  fun `verifyUsername returns true if oboConfig does not have a verification strategy`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(true, service.verifyUsername("testUsername", "consumer-name"))
  }

  @Test
  fun `verifyUsername returns false if oboConfig does have a verification strategy but user not verified`() {
    whenever(mockManageUsersService.usernameExists("testUsername", listOf("testStrategy"))).thenReturn(false)
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(oboConfig = OboConfig("test", verificationStrategy = "testStrategy")),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(false, service.verifyUsername("testUsername", "consumer-name"))
  }

  @Test
  fun `verifyUsername returns false if oboConfig does have a verification strategy and user IS verified`() {
    whenever(mockManageUsersService.usernameExists("testUsername", listOf("testStrategy"))).thenReturn(true)
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(oboConfig = OboConfig("test", verificationStrategy = "testStrategy")),
          ),
        ),
        mockTelemetryService,
        mockManageUsersService,
      )
    assertEquals(true, service.verifyUsername("testUsername", "consumer-name"))
  }

//  @DisplayName("Handle certificate expiry date")
//  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//  @Nested
//  inner class TestCertificateExpiry {
//    private val fixedClock: Clock = Clock.fixed(LocalDateTime.of(2026, 5, 8, 12, 30, 10).toInstant(ZoneOffset.UTC), ZoneId.systemDefault())
//
//    val authorisationService =
//      AuthorisationService(
//        AuthorisationConfig(),
//        mockTelemetryService,
//        mockManageUsersService,
//        fixedClock,
//      )
//
//    @BeforeEach
//    fun setUp() {
//      reset(mockTelemetryService)
//    }
//
//  }
}
