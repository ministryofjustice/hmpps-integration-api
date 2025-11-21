package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPndAlerts

class StatusInformationIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun setUp() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithPndAlerts
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Test
  fun `returns status information for a person`() {
    callApi("$basePath/$pnc/status-information")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-status-information")))
  }

  @Test
  fun `returns 403 when no WRSM configued in filters`() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    callApi("$basePath/$pnc/status-information")
      .andExpect(status().isForbidden)
  }
}
