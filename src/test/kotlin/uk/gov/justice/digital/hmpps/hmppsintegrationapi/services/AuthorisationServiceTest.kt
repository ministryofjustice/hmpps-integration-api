package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.matchers.collections.shouldHaveSize
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ConfigTest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig.OboConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.laoRedactionPolicy
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.dsl.role
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.UnsignedJwtOboService
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
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
        mockTelemetryService,
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
        mockTelemetryService
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
        mockTelemetryService
      )
    assertEquals(UnsignedJwtOboService()::class::java, service.oboService("consumer-name")!!::class::java)
  }

  @Test
  fun `returns null for entra oboConfig`() {
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
        mockTelemetryService
      )
    assertEquals(null, service.oboService("consumer-name"))
  }

  @Test
  fun `returns true if oboConfig has a value`() {
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
        mockTelemetryService
      )
    assertEquals(true, service.requiresObo("consumer-name"))
  }

  @Test
  fun `returns false if oboConfig has a value`() {
    val service =
      AuthorisationService(
        AuthorisationConfig(
          mapOf(
            "consumer-name" to
              ConsumerConfig(),
          ),
        ),
        mockTelemetryService
      )
    assertEquals(false, service.requiresObo("consumer-name"))
  }

  @DisplayName("Handle certificate expiry date")
  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class TestCertificateExpiry {
    private val fixedClock: Clock = Clock.fixed(LocalDateTime.of(2026, 5, 8, 12, 30, 10).toInstant(ZoneOffset.UTC), ZoneId.systemDefault())

    val authorisationService =
      AuthorisationService(
        AuthorisationConfig(),
        mockTelemetryService,
        fixedClock,
      )

    @BeforeEach
    fun setUp() {
      reset(mockTelemetryService)
    }

    @Test
    fun `does not alert for a 1 digit day cert-expiry-date that expires in over 30 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("Jun 8 12:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-06-08T12:30:10Z", dateString)
      verify(mockTelemetryService, times(0)).captureMessage(any())
    }

    @Test
    fun `alerts a 2 digit day cert-expiry-date that expires in 30 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("Jun 7 12:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-06-07T12:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 30 days (Jun 7 12:30:10 2026 GMT)")
    }

    @Test
    fun `alerts a cert-expiry-date that expires in 21 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 29 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-29T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 3 weeks (May 29 00:30:10 2026 GMT)")
    }

    @Test
    fun `alerts for a cert-expiry-date that expires in 20 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 28 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-28T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 3 weeks (May 28 00:30:10 2026 GMT)")
    }

    @Test
    fun `alerts for a cert-expiry-date that expires in 14 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 22 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-22T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 2 weeks (May 22 00:30:10 2026 GMT)")
    }

    @Test
    fun `does not alert for a cert-expiry-date that expires in 12 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 20 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-20T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in under 2 weeks (May 20 00:30:10 2026 GMT)")
    }

    @Test
    fun `alerts for a cert-expiry-date that expires in 7 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 15 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-15T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 7 days (May 15 00:30:10 2026 GMT)")
    }

    @Test
    fun `alerts for a cert-expiry-date header that expires in 1 day`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 9 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-09T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 1 day (May 9 00:30:10 2026 GMT)")
    }

    @Test
    fun `alerts for a cert-expiry-date header that expires in 0 days`() {
      val dateString = authorisationService.processCertificateExpiryDate("May 8 00:30:10 2026 GMT", "consumer-name")
      assertEquals("2026-05-08T00:30:10Z", dateString)
      verify(mockTelemetryService, times(1)).captureMessage("The certificate for consumer-name will expire in 0 days (May 8 00:30:10 2026 GMT)")
    }

    @Test
    fun `throws an exception if already expired`() {
      val exception =
        assertThrows<RuntimeException> {
          authorisationService.processCertificateExpiryDate("May 7 00:30:10 2026 GMT", "consumer-name")
        }
      assertThat(exception.message).isEqualTo("The certificate for consumer-name with expiry date May 7 00:30:10 2026 GMT has expired")
    }

    @Test
    fun `handles an invalid format cert-expiry-date header and logs to sentry`() {
      val dateString = authorisationService.processCertificateExpiryDate("Wrong format", "consumer-name")
      assertEquals(null, dateString)
      val exception = argumentCaptor<Throwable>()
      verify(mockTelemetryService, times(1)).captureException(exception.capture())
      assertThat(exception.firstValue.message).contains("Failed to parse certificate expiry date")
    }

    @Test
    fun `does not create a message for days over 30`() {
      val warningMessage = authorisationService.expiryWarningMessage(31, "May 7 00:30:10 2026 GMT", "consumer-name")
      assertThat(warningMessage).isEqualTo(null)
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationServiceTest#expiryBandTestArgs")
    fun `creates the same message for each band`(
      days: List<Long>,
      expectedNumberOfDistinctMessages: Int,
    ) {
      val certificateExpiry = "May 7 00:30:10 2026 GMT"
      val messages = days.map { days -> authorisationService.expiryWarningMessage(days, certificateExpiry, "consumer-name") }
      messages.toSet().shouldHaveSize(expectedNumberOfDistinctMessages)
    }

    @Test
    fun `throws an exception when days are negative`() {
      val exception =
        assertThrows<RuntimeException> {
          authorisationService.expiryWarningMessage(-1, "May 7 00:30:10 2026 GMT", "consumer-name")
        }
      assertThat(exception.message).isEqualTo("The certificate for consumer-name with expiry date May 7 00:30:10 2026 GMT has expired")
    }
  }
}
