package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.slf4j.LoggerFactory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerConfig
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class PermissionCheckerTest {
  val log = LoggerFactory.getLogger(this.javaClass)
  val permissionChecker = PermissionChecker()

  @Test
  fun `has permission`() {
    assertTrue(permissionChecker.hasPermission("/v1/status", "dev", "smoke-test-full-access"))
  }

  @Test
  fun `does not have permission`() {
    Assertions.assertFalse(permissionChecker.hasPermission("/v9/status", "dev", "smoke-test-limited-access"))
  }

  @Test
  fun `no matching environment`() {
    assertEquals(0, permissionChecker.consumersWithPermission("/v1/status", "notfound").size)
  }

  @Test
  fun `multiple matching consumers`() {
    for (env in listOf("dev", "preprod", "prod")) {
      val matches = permissionChecker.consumersWithPermission("/v1/status", env)
      assertNotEquals(0, matches.size)
      assertContains(matches, "event-service")
      assertContains(matches, "kubernetes-health-check-client")
    }
  }

  @Test
  fun `show permission matches`() {
    val endpoint = "/health/readiness"
    val environment = "preprod"
    val matches = permissionChecker.consumersWithPermission(endpoint, "preprod")

    log.info("Consumers with access to {} in {} : {}", endpoint, environment, matches)

    assertEquals(1, matches.size)
    assertEquals("kubernetes-health-check-client", matches[0])
  }

  @Test
  fun `validate core PermissionChecker behaviour`() {
    val authConfig = AuthorisationConfig()
    authConfig.consumers =
      mapOf(
        "c1" to ConsumerConfig(include = listOf("/tester"), filters = null, roles = listOf()),
        "c2" to ConsumerConfig(include = listOf("/tester", "/other"), filters = null, roles = listOf()),
        "c3" to ConsumerConfig(include = listOf("/other"), filters = null, roles = listOf()),
      )
    val provider = mock(AuthorisationConfigProvider::class.java)
    whenever(provider.getConfig(any())).thenReturn(authConfig)

    val matches = PermissionChecker(provider).consumersWithPermission("/tester", "test1")
    assertEquals(2, matches.size)
    assertContains(matches, "c1")
    assertContains(matches, "c2")
  }
}
