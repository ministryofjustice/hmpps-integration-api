package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.Offender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.OtherIds
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonAndProbationSupervisionFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonOnlySupervisionFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithProbationOnlySupervisionFilters
import java.io.File

class SupervisionStatusIntegrationTest : IntegrationTestBase() {
  val path = "$basePath/$crn"

  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
  }

  @BeforeEach
  fun resetMocks() {
    nDeliusMockServer.resetAll()
    prisonerOffenderSearchMockServer.resetAll()
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsId",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdFromProbation",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )

    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("crn" to crn)),
      writeAsJson(Offender(firstName = "firstName", surname = "lastName", activeProbationManagedSentence = true, otherIds = OtherIds(nomsNumber = nomsId))),
    )
  }

  @Test
  fun `throws a 403 if probation record is NOT an active supervision and ONLY PROBATION exists in the filter`() {
    whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithProbationOnlySupervisionFilters))
    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("crn" to crn)),
      writeAsJson(Offender(firstName = "firstName", surname = "lastName", activeProbationManagedSentence = false)),
    )
    val response =
      callApi(path)
        .andExpect(status().isForbidden)
        .andReturn()
        .response
        .contentAsJson<ErrorResponse>()
    response.userMessage.shouldBe("Not under active supervision. Access denied.")
  }

  @Test
  fun `ONLY probation data and no prisons data is returned when probation record is an active supervision and only PROBATION exists in the filter`() {
    whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithProbationOnlySupervisionFilters))
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.data.prisonerOffenderSearch").doesNotExist())
      .andExpect(jsonPath("$.data.probationOffenderSearch").exists())
  }

  @Test
  fun `BOTH probation data and prisons data is returned if probation record found with active supervision with BOTH PRISONS and PROBATION exists in the filter`() {
    whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithPrisonAndProbationSupervisionFilters))
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.data.prisonerOffenderSearch").exists())
      .andExpect(jsonPath("$.data.probationOffenderSearch").exists())
  }

  @Test
  fun `BOTH probation data and prisons data is returned if probation record in not an active supervision and only PRISONS exists in filter`() {
    whenever(authorisationConfig.roles).thenReturn(mapOf("full-access" to testRoleWithPrisonOnlySupervisionFilters))
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(jsonPath("$.data.prisonerOffenderSearch").exists())
      .andExpect(jsonPath("$.data.probationOffenderSearch").exists())
  }
}
