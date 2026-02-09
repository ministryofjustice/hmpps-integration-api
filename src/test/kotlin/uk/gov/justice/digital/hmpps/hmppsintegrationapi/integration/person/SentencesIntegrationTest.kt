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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import java.io.File

class SentencesIntegrationTest : IntegrationTestBase() {
  final var path = "$basePath/$crn/sentences"
  final var invalidHmppsIdPath = "$basePath/INVALID/sentences"

  @BeforeEach
  fun setUp() {
    nDeliusMockServer.stubForGet(
      "/case/$crn/supervisions",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetSupervisionsResponse.json",
      ).readText(),
    )
  }

  @AfterEach
  fun tearDown() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Test
  fun `returns sentences for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence")))
  }

  @Test
  fun `sentences returns a 400 if the hmppsId is invalid`() {
    callApi(invalidHmppsIdPath)
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `sentences returns a 404 for if consumer has empty list of prisons on latest sentence key dates and adjustments `() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `sentences returns a 404 for prisoner in wrong prison on latest sentence key dates and adjustments`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns latest sentence key dates and adjustments for a person`() {
    callApi("$path/latest-key-dates-and-adjustments")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence-key-dates")))
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$invalidHmppsIdPath/latest-key-dates-and-adjustments")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `return a 404 for if consumer has empty list of prisons on latest sentence key dates and adjustments `() {
    callApiWithCN("$path/latest-key-dates-and-adjustments", noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `return a 404 for prisoner in wrong prison on latest sentence key dates and adjustments`() {
    callApiWithCN("$path/latest-key-dates-and-adjustments", limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `sentences returns forbidden for an LAO person when redaction policy is present`() {
    setToLao()
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithLaoRedactions
    callApi(path)
      .andExpect(status().isForbidden)
  }

  @Test
  fun `returns latest sentence key dates and adjustments for an LAO person when redaction policy is present`() {
    setToLao()
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithLaoRedactions
    callApi("$path/latest-key-dates-and-adjustments")
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-sentence-key-dates")))
  }
}
