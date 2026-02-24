package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class PrisonerContactsIntegrationTest : IntegrationTestBase() {
  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Nested
  inner class GetPrisonerContacts {
    @Test
    fun `returns a prisoners contacts`() {
      val params = "?page=1&size=10"
      callApi("$basePath/$nomsId/contacts$params")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("prisoners-contacts")))
    }
  }

  @Nested
  inner class GetPrisonerEmergencyContacts {
    @Test
    fun `returns a prisoner's emergency contacts`() {
      val params = "?page=1&size=10"
      callApi("$basePath/$nomsId/emergency-contacts$params")
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("prisoners-contacts")))
    }
  }
}
