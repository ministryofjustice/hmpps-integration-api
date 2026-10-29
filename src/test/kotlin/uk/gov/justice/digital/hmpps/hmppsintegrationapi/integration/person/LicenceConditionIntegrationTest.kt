package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.CHECK_NDELIUS_WHEN_NO_CVL_LICENCES
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class LicenceConditionIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun beforeEach() {
    whenever(featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES)).thenReturn(false)
    createAndVaryLicenceServer.stubForGet(
      "/public/licence-summaries/crn/$crn",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/createAndVaryLicence/fixtures/PersonLicenceSummaries.json",
      ).readText(),
    )
  }

  @Test
  fun `returns licence conditions from CVL for a person`() {
    callApi("$basePath/$nomsId/licences/conditions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-licence-conditions.json"), JsonCompareMode.STRICT))
  }

  @Test
  fun `returns licence conditions from NDelius for a person`() {
    whenever(featureFlagConfig.isEnabled(CHECK_NDELIUS_WHEN_NO_CVL_LICENCES)).thenReturn(true)
    createAndVaryLicenceServer.stubForGet(
      "/public/licence-summaries/crn/$crn",
      "[]",
    )
    nDeliusMockServer.stubForGet(
      "/case/$crn/licence-conditions",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/LicenceConditionsResponse.json",
      ).readText(),
    )

    callApi("$basePath/$nomsId/licences/conditions")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-licence-conditions-ndelius-only.json"), JsonCompareMode.STRICT))
  }
}
