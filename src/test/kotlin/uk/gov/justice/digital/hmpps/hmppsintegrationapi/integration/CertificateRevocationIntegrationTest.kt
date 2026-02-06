package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test

@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
class CertificateRevocationIntegrationTest : IntegrationTestBase() {
  @Test
  fun `revokes certificate specifically for consumer`() {
    val response =
      callApiWithCN("/v1/hmpps/reference-data", "automated-test-client", serialNumber = "1")
        .andExpect(status().isForbidden)
        .andReturn()
    assertThat(response.response.errorMessage).isEqualTo("Certificate with serial number 01 has been revoked")
  }

  @Test
  fun `revokes certificate regardless of consumer`() {
    val response =
      callApiWithCN("/v1/hmpps/reference-data", "automated-test-client", serialNumber = revokedSerialNumber)
        .andExpect(status().isForbidden)
        .andReturn()
    assertThat(response.response.errorMessage).isEqualTo("Certificate with serial number 01:7B:EB:77:06:DB:11:F5:2E:B6:F7:37:7B:A9:E0:E4:84:C5:2C:A3 has been revoked")
  }
}
