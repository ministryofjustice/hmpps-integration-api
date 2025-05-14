package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class RolesIncludeIntegrationTest : IntegrationTestBase() {
  @Test
  fun `should get 200 when path is in the role includes`() {
    callApiWithCN("/v1/prison/prisoners?first_name=Robert", "private-prison-only")
      .andExpect(status().isOk)
  }

  @Test
  fun `should get 403 when path is not in role includes`() {
    callApiWithCN("/path-not-available-to-private-prison", "private-prison-only")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `reference-data-only should be able to access reference data`() {
    // There is a defect (HIA-788) in the reference data service / NOMIS prism
    // mock that results in an internal server error. The expected response for
    // this test should be changed to 200 as part of fixing that defect.
    // In the meantime, the 500 demonstrates that the client is able to invoke
    // the endpoint, which is the purpose of this test.
    callApiWithCN("/v1/hmpps/reference-data", "reference-data-only-user")
      .andExpect(status().isInternalServerError)
  }

  @Test
  fun `reference-data-only should not be able to access other endpoint`() {
    callApiWithCN("/v1/prison/prisoners?first_name=Test", "reference-data-only-user")
      .andExpect(status().isForbidden)
  }
}
