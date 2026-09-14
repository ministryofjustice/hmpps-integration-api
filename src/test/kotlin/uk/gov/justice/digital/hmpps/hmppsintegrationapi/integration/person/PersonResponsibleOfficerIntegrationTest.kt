package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import java.io.File

class PersonResponsibleOfficerIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun setup() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.INCLUDE_PROVIDER_IN_RESPONSIBLE_OFFICER_TEAM)).thenReturn(true)
  }

  @AfterEach
  fun resetValidators() {
    managePomCaseMockServer.resetValidator()
  }

  @Test
  fun `returns responsible officer for a person`() {
    managePomCaseMockServer.stubForGet(
      "/api/allocation/$nomsId/primary_pom",
      File("$gatewaysFolder/managePOMcase/fixtures/GetPrimaryPOMResponse.json").readText(),
    )
    callApi("$basePath/$nomsId/person-responsible-officer")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer"), JsonCompareMode.STRICT))

    managePomCaseMockServer.assertValidationPassed()
  }

  @Test
  fun `returns responsible officer without provider if feature is not enabled`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.INCLUDE_PROVIDER_IN_RESPONSIBLE_OFFICER_TEAM)).thenReturn(false)
    managePomCaseMockServer.stubForGet(
      "/api/allocation/$nomsId/primary_pom",
      File("$gatewaysFolder/managePOMcase/fixtures/GetPrimaryPOMResponse.json").readText(),
    )
    callApi("$basePath/$nomsId/person-responsible-officer")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer-no-provider.json"), JsonCompareMode.STRICT))

    managePomCaseMockServer.assertValidationPassed()
  }

  @Test
  fun `adjudications returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/person-responsible-officer")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 for person in wrong prison`() {
    callApiWithCN("$basePath/$nomsId/person-responsible-officer", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 when no prisons in filter`() {
    callApiWithCN("$basePath/$nomsId/person-responsible-officer", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns redacted responsible officer for a person`() {
    managePomCaseMockServer.stubForGet(
      "/api/allocation/$nomsIdFromProbation/primary_pom",
      File("$gatewaysFolder/managePOMcase/fixtures/GetPrimaryPOMResponse.json").readText(),
    )
    callApiWithCN("$basePath/$nomsIdFromProbation/person-responsible-officer", "ext-probation-police-intelligence")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer-redacted.json"), JsonCompareMode.STRICT))

    managePomCaseMockServer.assertValidationPassed()
  }

  @Test
  fun `returns only the community officer for a person who is in prison when the consumer has a supervision status of PROBATION`() {
    nDeliusMockServer.stubForGet(
      "/case/${Companion.crnNotActiveInProbation}/supervisions",
      File(
        "$gatewaysFolder/ndelius/fixtures/SupervisionsResponse.json",
      ).readText(),
    )

    callApiWithCN("$basePath/$crnNotActiveInProbation/person-responsible-officer", "ext-probation-police-intelligence")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-responsible-officer-redacted.json"), JsonCompareMode.STRICT))
  }
}
