package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase {
  @Autowired
  lateinit var mockMvc: MockMvc

  final val basePath = "/v1/persons"
  final val pnc = URLEncoder.encode("2004/13116M", StandardCharsets.UTF_8)
  final val nomsId = "G2996UX"
  final val crn = "AB123123"
  final val nomsIdNotInDelius = "A1234AA"

  companion object {
    private val hmppsAuthMockServer = HmppsAuthMockServer()

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret")
    }

    @AfterAll
    @JvmStatic
    fun stopMockServers() {
      hmppsAuthMockServer.stop()
    }
  }

  fun getAuthHeader(): HttpHeaders {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=automated-test-client")
    return headers
  }

  fun getExpectedResponse(filename: String): String = File("./src/test/resources/expected-responses/$filename").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

  fun callApi(path: String): ResultActions =
    mockMvc.perform(
      get(path).headers(getAuthHeader()),
    )
}
