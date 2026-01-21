package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Address
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonAndProbationSupervisionFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithPrisonOnlySupervisionFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithProbationOnlySupervisionFilters

class AddressIntegrationTest : IntegrationTestBase() {
  val path = "$basePath/$nomsId/addresses"

  @AfterEach
  fun setup() {
    unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
  }

  @Test
  fun `returns addresses for a person`() {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(content().json(getExpectedResponse("person-addresses")))
  }

  @Test
  fun `returns probation addresses only when only PROBATION supervision status`() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithProbationOnlySupervisionFilters
    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(1)
  }

  @Test
  fun `returns prison addresses only when only PRISON supervision status`() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithPrisonOnlySupervisionFilters
    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(1)
  }

  @Test
  fun `returns probation and prison addresses when PRISON and PROBATION supervision status`() {
    mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
    every { roles[any()] } returns testRoleWithPrisonAndProbationSupervisionFilters
    val response =
      callApi(path)
        .andExpect(status().isOk)
        .andReturn()
        .response
        .contentAsJson<Response<List<Address>>>()
    assertThat(response.data.size).isEqualTo(2)
  }

  @Test
  fun `returns a 400 if the hmppsId is invalid`() {
    callApi("$basePath/$invalidNomsId/addresses")
      .andExpect(status().isBadRequest)
  }

  @Test
  fun `returns a 404 for if consumer has empty list of prisons`() {
    callApiWithCN(path, noPrisonsCn)
      .andExpect(status().isNotFound)
  }

  @Test
  fun `returns a 404 for prisoner in wrong prison`() {
    callApiWithCN(path, limitedPrisonsCn)
      .andExpect(status().isNotFound)
  }
}
