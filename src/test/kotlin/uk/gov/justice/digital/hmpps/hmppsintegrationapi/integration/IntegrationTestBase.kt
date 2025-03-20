package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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
  final val defaultCn = "automated-test-client"
  final val pnc = URLEncoder.encode("2004/13116M", StandardCharsets.UTF_8)
  final val nomsId = "G2996UX"
  final val invalidNomsId = "G2996UXX"
  final val crn = "AB123123"
  final val nomsIdNotInDelius = "A1234AA"
  final val limitedPrisonsCn = "limited-prisons"
  final val noPrisonsCn = "no-prisons"
  final val emptyPrisonsCn = "empty-prisons"
  final val contactId = 123456L

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

  fun getAuthHeader(cn: String = defaultCn): HttpHeaders {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=$cn")
    return headers
  }

  fun getExpectedResponse(filename: String): String = File("./src/test/resources/expected-responses/$filename").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

  fun callApi(path: String): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader()))

  fun callApiWithCN(
    path: String,
    cn: String,
  ): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader(cn)))

  fun postToApi(
    path: String,
    requestBody: String,
  ): ResultActions =
    mockMvc.perform(
      post(path)
        .headers(getAuthHeader())
        .content(requestBody)
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON),
    )

  fun postToApiWithCN(
    path: String,
    requestBody: String,
    cn: String,
  ): ResultActions =
    mockMvc.perform(
      post(path)
        .headers(getAuthHeader(cn))
        .content(requestBody)
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON),
    )

  fun asJsonString(obj: Any): String {
    val objectMapper = ObjectMapper()
    objectMapper.registerModule(JavaTimeModule())
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    return objectMapper.writeValueAsString(obj)
  }
}
