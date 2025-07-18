package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

class HealthAndDietIntegrationTest : IntegrationTestBase() {
  val healthAndMedicationMockServer = ApiMockServer.create(UpstreamApi.HEALTH_AND_MEDICATION)

  @BeforeEach
  fun start() {
    healthAndMedicationMockServer.start()
  }

  @AfterEach
  fun reset() {
    healthAndMedicationMockServer.stop()
    healthAndMedicationMockServer.resetValidator()
  }

  @Test
  fun `returns health and diet information for a person`() {
    healthAndMedicationMockServer.stubForGet(
      "/prisoners/$nomsId",
      File(
        "$gatewaysFolder/healthandmedication/fixtures/GetHealthAndMedicationResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/health-and-diet")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-health-and-diet")))

    healthAndMedicationMockServer.assertValidationPassed()
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/health-and-diet", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/health-and-diet", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 400 when invalid noms passed in`() {
    callApi("$basePath/$invalidNomsId/health-and-diet")
      .andExpect(status().isBadRequest)
  }
}
