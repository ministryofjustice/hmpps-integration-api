package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.createUnsignedJwt
import kotlin.test.Test

class OnBehalfOfIntegrationTest : IntegrationTestBase() {
  @Test
  fun `if oboConfig is empty, and no Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-empty")
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is empty, and a Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-empty", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is unsigned, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-unsigned")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is unsigned, and a Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is entra, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-entra")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is entra, and a Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-entra", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is invalid, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-invalid")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is invalid, and a Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-invalid", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }
}
