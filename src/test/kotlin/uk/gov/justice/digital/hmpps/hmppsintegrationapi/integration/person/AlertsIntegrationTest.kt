package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase

class AlertsIntegrationTest : IntegrationTestBase() {
  @Nested
  inner class GetAlerts {
    val path = "$basePath/$nomsId/alerts"

    @Test
    fun `returns alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("$basePath/$invalidNomsId/alerts")
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

  @Nested
  inner class GetAlertsPnd {
    val path = "$basePath/$nomsId/alerts/pnd"

    @Test
    fun `returns PND alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts-pnd")))
    }

    @Test
    fun `returns a 400 if the hmppsId is invalid`() {
      callApi("$basePath/$invalidNomsId/alerts/pnd")
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

  @Nested
  inner class GetPndAlerts {
    val path = "/v1/pnd/persons/$nomsId/alerts"

    @Test
    fun `returns PND alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts-pnd")))
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
