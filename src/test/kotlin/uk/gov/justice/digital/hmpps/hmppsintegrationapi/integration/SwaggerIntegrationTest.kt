package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SwaggerIntegrationTest : IntegrationTestBase() {
  private val cn = "full-access"

  @ParameterizedTest
  @ValueSource(
    strings = [
      "/v3/api-docs",
//      "/v3/api-docs/swagger-config",      // Swagger UI is not accessible currently.
//      "/swagger-ui/index.html",
//      "/swagger-ui/swagger-ui.css",
    ],
  )
  fun `swagger endpoints are accessible`(path: String) {
    callApiWithCN(path, cn)
      .andExpect(status().is2xxSuccessful)
  }

  @Disabled("Swagger UI is not accessible currently.")
  @Test
  fun `swagger redirection is accessible`() {
    val path = "/swagger-ui.html"
    callApiWithCN(path, cn)
      .andExpect(status().is3xxRedirection)
  }
}
