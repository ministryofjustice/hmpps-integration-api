package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPndAlerts
import java.io.File

class AlertsIntegrationTest : IntegrationTestBase() {
  @Nested
  inner class GetAlerts {
    val path = "$basePath/$nomsId/alerts"
    val activeOnlyPath = "$basePath/$nomsId/active-alerts"

    @Test
    fun `returns all alerts for a person`() {
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))

      verify(alertsGateway, times(1)).getPrisonerAlertsForCodes(nomsId, 1, 10, emptyList(), false)
    }

    @Test
    fun `returns active alerts for a person`() {
      callApi(activeOnlyPath)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))

      verify(alertsGateway, times(1)).getPrisonerAlertsForCodes(nomsId, 1, 10, emptyList(), true)
    }

    @Test
    fun `returns alerts for a person with alert filters`() {
      whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithPndAlerts))
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-alerts")))

      verify(alertsGateway, times(1)).getPrisonerAlertsForCodes(nomsId, 1, 10, testRoleWithPndAlerts.filters?.alertCodes!!, false)
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

  @Nested
  inner class AlertsLaoRedactions {
    val crn = "A123456"
    val path = "/v1/persons/$crn/alerts"

    @Test
    fun `returns Redacted alerts for an LAO`() {
      corePersonRecordMockServer.stubForGet(
        "/person/probation/$crn",
        File(
          "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
        ).readText(),
      )
      nDeliusMockServer.stubForPost(
        "/probation-cases/access",
        """
          {
            "crns": ["$crn"]
          }
          """.removeWhitespaceAndNewlines(),
        """
        {
          "access": [{
            "crn": "$crn",
            "userExcluded": true,
            "userRestricted": false
          }]
        }
        """.trimIndent(),
      )
      whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithLaoRedactions))
      callApi(path)
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.data[*].offenderNo").exists())
        .andExpect(jsonPath("$.data[*].type").doesNotExist())
        .andExpect(jsonPath("$.data[*].typeDescription").doesNotExist())
        .andExpect(jsonPath("$.data[*].code").exists())
        .andExpect(jsonPath("$.data[*].codeDescription").exists())
        .andExpect(jsonPath("$.data[*].comment").exists())
        .andExpect(jsonPath("$.data[*].dateCreated").exists())
        .andExpect(jsonPath("$.data[*].dateExpired").doesNotExist())
        .andExpect(jsonPath("$.data[*].expired").doesNotExist())
        .andExpect(jsonPath("$.data[*].active").doesNotExist())
    }
  }
}
