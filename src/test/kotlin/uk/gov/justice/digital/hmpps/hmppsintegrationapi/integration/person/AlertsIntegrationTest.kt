package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.fullAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPndAlerts

class AlertsIntegrationTest : IntegrationTestBase() {
  @MockitoSpyBean
  private lateinit var alertsGateway: PrisonerAlertsGateway

  @Nested
  inner class GetAlerts {
    val path = "$basePath/$nomsId/alerts"

    @BeforeEach
    fun setup() {
      mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      every { roles.get(any()) } returns fullAccess
    }

    @AfterEach
    fun tearDown() {
      unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    }

    @Test
    fun `returns alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))
    }

    @Test
    fun `returns alerts for a person with alert filters`() {
      every { roles.get(any()) } returns testRoleWithPndAlerts
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))

      verify(alertsGateway, times(1)).getPrisonerAlertsForCodes(nomsId, 1, 10, testRoleWithPndAlerts.filters?.alertCodes!!)
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("$basePath/$invalidNomsId/alerts")
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }

  @Nested
  inner class GetPndAlerts {
    val path = "/v1/pnd/persons/$nomsId/alerts"

    @Test
    fun `returns PND alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("/v1/pnd/persons/$invalidNomsId/alerts")
        .andExpect(status().isBadRequest)
    }

    @Test
    fun `return a 404 for person in wrong prison`() {
      callApiWithCN(path, limitedPrisonsCn)
        .andExpect(status().isNotFound)
    }

    @Test
    fun `return a 404 when no prisons in filter`() {
      callApiWithCN(path, noPrisonsCn)
        .andExpect(status().isNotFound)
    }
  }
}
