package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import kotlin.test.Test
import kotlin.test.assertEquals

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class LocalProfileIntegrationTest {
  @Autowired
  lateinit var authorisationService: AuthorisationService

  @Autowired
  lateinit var mockMvc: MockMvc

  @Test
  fun `Check default profile can access apidocs`() {
    mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk)
    assertEquals(authorisationService.defaultConsumerName(), "all-access")
  }
}
