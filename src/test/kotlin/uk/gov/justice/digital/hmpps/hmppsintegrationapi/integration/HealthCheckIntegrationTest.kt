package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class HealthCheckIntegrationTest : IntegrationTestBase() {
  @ParameterizedTest
  @ValueSource(strings = ["/health", "/health/ping", "/health/readiness"])
  fun `health check test`(path: String) {
    callApi(path)
      .andExpect(status().isOk)
      .andExpect(
        content().json(
          """
        {"status":"UP"}
        """,
        ),
      )
  }
}
