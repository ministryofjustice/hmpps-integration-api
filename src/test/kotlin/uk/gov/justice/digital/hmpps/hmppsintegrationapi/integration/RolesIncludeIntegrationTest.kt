package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RolesIncludeIntegrationTest : IntegrationTestBase() {
  @Test
  fun `should get 200 when path is in the role includes`() {
    callApiWithCN("/v1/prison/prisoners/$nomsId", "private-prison-only")
      .andExpect(status().isOk)
  }

  @Test
  fun `should get 403 when path is not in role includes`() {
    callApiWithCN("/path-not-available-to-private-prison", "private-prison-only")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `reference-data-only should be able to access reference data`() {
    callApiWithCN("/v1/hmpps/reference-data", "reference-data-only-user")
      .andExpect(status().isOk)
  }

  @Test
  fun `reference-data-only should not be able to access other endpoint`() {
    callApiWithCN("/v1/prison/prisoners?first_name=Test", "reference-data-only-user")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `person-search-no-access-client should NOT be able to access search endpoint`() {
    callApiWithCN("/v1/persons?first_name=John", "person-search-no-access-client")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `person-search-no-access-client should be able to access single persons endpoint`() {
    callApiWithCN("/v1/persons/$crn", "person-search-no-access-client")
      .andExpect(status().isOk)
  }

  @Test
  fun `person-search-no-access-client should be able to access persons address endpoint`() {
    callApiWithCN("/v1/persons/$crn/addresses", "person-search-no-access-client")
      .andExpect(status().isOk)
  }
}
