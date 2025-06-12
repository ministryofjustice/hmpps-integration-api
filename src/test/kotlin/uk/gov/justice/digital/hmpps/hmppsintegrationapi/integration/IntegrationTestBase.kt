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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
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
  final val specificPrisonCn = "specific-prison"
  final val limitedPrisonsCn = "limited-prisons"
  final val noPrisonsCn = "no-prisons"
  final val emptyPrisonsCn = "empty-prisons"
  final val contactId = 123456L

  companion object {
    private val nomsId = "G2996UX"
    private val nomsIdFromProbation = "G5555TT"

    val gatewaysFolder = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways"
    private val hmppsAuthMockServer = HmppsAuthMockServer()
    val prisonerOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)
    val healthAndMedicationMockServer = ApiMockServer.create(UpstreamApi.HEALTH_AND_MEDICATION)
    val managePomCaseMockServer = ApiMockServer.create(UpstreamApi.MANAGE_POM_CASE)
    val plpMockServer = ApiMockServer.create(UpstreamApi.PLP)
    val activitiesMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret")

      prisonerOffenderSearchMockServer.start()
      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/$nomsId",
        File(
          "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
        ).readText(),
      )
      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/$nomsIdFromProbation",
        File(
          "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
        ).readText(),
      )

      healthAndMedicationMockServer.start()
      healthAndMedicationMockServer.stubForGet(
        "/prisoners/$nomsId",
        File(
          "$gatewaysFolder/healthandmedication/fixtures/GetHealthAndMedicationResponse.json",
        ).readText(),
      )

      managePomCaseMockServer.start()
      managePomCaseMockServer.stubForGet(
        "/api/allocation/$nomsId/primary_pom",
        File("$gatewaysFolder/managePOMcase/fixtures/GetPrimaryPOMResponse.json").readText(),
      )

      plpMockServer.start()

      activitiesMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMockServers() {
      hmppsAuthMockServer.stop()
      prisonerOffenderSearchMockServer.stop()
      healthAndMedicationMockServer.stop()
      managePomCaseMockServer.stop()
      plpMockServer.stop()
      activitiesMockServer.stop()
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

  fun putApi(
    path: String,
    requestBody: String,
  ): ResultActions =
    mockMvc.perform(
      put(path)
        .headers(getAuthHeader())
        .content(requestBody)
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON),
    )

  fun putApiWithCN(
    path: String,
    requestBody: String,
    cn: String,
  ): ResultActions =
    mockMvc.perform(
      put(path)
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
