package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.reset
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues.Queue
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues.QueueProvider
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.queues.TestQueue
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestConstants.DEFAULT_CRN
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class IntegrationTestBase {
  @MockitoSpyBean
  lateinit var featureFlagConfig: FeatureFlagConfig

  @MockitoSpyBean
  lateinit var alertsGateway: PrisonerAlertsGateway

  @MockitoSpyBean
  lateinit var activitiesGateway: ActivitiesGateway

  @MockitoSpyBean
  lateinit var telemetryService: TelemetryService

  @MockitoSpyBean
  lateinit var prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway

  @MockitoSpyBean
  lateinit var corePersonRecordGateway: CorePersonRecordGateway

  @MockitoSpyBean
  lateinit var nDeliusGateway: NDeliusGateway

  @MockitoSpyBean
  lateinit var authorisationConfig: AuthorisationConfig

  @MockitoSpyBean
  lateinit var eventNotificationRepository: JdbcTemplateEventNotificationRepository

  @Autowired
  lateinit var mockMvc: MockMvc

  @Autowired
  lateinit var cacheManager: CacheManager

  @Autowired
  lateinit var queueProvider: QueueProvider

  @BeforeEach
  fun evictAllCaches() {
    reset(alertsGateway)
    reset(telemetryService)
    reset(activitiesGateway)
    reset(prisonerOffenderSearchGateway)
    reset(corePersonRecordGateway)
    reset(nDeliusGateway)
    reset(featureFlagConfig)
    reset(authorisationConfig)
    reset(eventNotificationRepository)

    cacheManager.cacheNames.forEach {
      cacheManager.getCache(it).clear()
    }

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/${Companion.nomsId}",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/A1234AA",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponseA1234AA.json",
      ).readText(),
    )
  }

  final val basePath = "/v1/persons"
  final val defaultCn = "automated-test-client"
  final val pnc = URLEncoder.encode("2004/13116M", StandardCharsets.UTF_8)
  final val nomsId = "G2996UX"
  final val invalidNomsId = "G2996UXX"
  final val crn = DEFAULT_CRN
  final val specificPrisonCn = "specific-prison"
  final val limitedPrisonsCn = "limited-prisons"
  final val limitedCaseNotesCn = "limited-case-notes"
  final val noPrisonsCn = "no-prisons"
  final val emptyPrisonsCn = "empty-prisons"
  final val noProbationAccessCn = "supervision-status-prison-only"
  final val contactId = 123456L
  final val nomsIdFromProbation = "G5555TT"

  companion object {
    private val nomsId = "G2996UX"
    private val nomsIdFromProbation = "G5555TT"
    private val crn = DEFAULT_CRN

    val certSerialNumber = "9572494320151578633330348943480876283449388176"
    val revokedSerialNumber = "8472494320151578633330348943480876283449388195"

    val gatewaysFolder = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways"
    private val hmppsAuthMockServer = HmppsAuthMockServer()
    val prisonerOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)
    val managePomCaseMockServer = ApiMockServer.create(UpstreamApi.MANAGE_POM_CASE)
    val plpMockServer = ApiMockServer.create(UpstreamApi.PLP)
    val sanMockServer = ApiMockServer.create(UpstreamApi.SAN)
    val activitiesMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
    val nDeliusMockServer = ApiMockServer.create(UpstreamApi.NDELIUS_INTEGRATION_TEST)
    val prisonerBaseLocationMockServer = ApiMockServer.create(UpstreamApi.PRISONER_BASE_LOCATION)
    val corePersonRecordMockServer = ApiMockServer.create(UpstreamApi.CORE_PERSON_RECORD)
    val arnsMockServer = ApiMockServer.create(UpstreamApi.ARNS_INTEGRATION_TEST)

    @BeforeEach
    fun setUp() {
    }

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      hmppsAuthMockServer.start()
      corePersonRecordMockServer.start()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", HmppsAuthMockServer.TOKEN)

      prisonerOffenderSearchMockServer.start()

      prisonerOffenderSearchMockServer.stubForGet(
        "/prisoner/$nomsIdFromProbation",
        File(
          "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdProbationResponse.json",
        ).readText(),
      )
      prisonerBaseLocationMockServer.stubForGet(
        "/v1/persons/$nomsId/prisoner-base-location",
        File(
          "$gatewaysFolder/prisonerbaselocation/fixtures/PrisonerBaseLocationResponse.json",
        ).readText(),
      )
      corePersonRecordMockServer.stubForGet(
        "/person/prison/$nomsId",
        File(
          "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
        ).readText(),
      )
      corePersonRecordMockServer.stubForGet(
        "/person/probation/$crn",
        File(
          "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
        ).readText(),
      )
      nDeliusMockServer.start()

      nDeliusMockServer.stubForPost(
        "/probation-cases/access",
        """
          {
            "crns": ["$crn"]
          }
          """.removeWhitespaceAndNewlines(),
        """
        {
          "access": [{
            "crn": "$crn",
            "userExcluded": false,
            "userRestricted": false
          }]
        }
        """.trimIndent(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("crn" to crn)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponse.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("nomsNumber" to nomsId)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponse.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("nomsNumber" to nomsIdFromProbation)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponse.json",
        ).readText(),
      )

      nDeliusMockServer.stubForGet(
        "/case/$crn/addresses",
        File(
          "$gatewaysFolder/ndelius/fixtures/GetAddressesResponse.json",
        ).readText(),
      )

      nDeliusMockServer.stubForGet(
        "/case/$crn/supervisions",
        File(
          "$gatewaysFolder/ndelius/fixtures/SupervisionsResponse.json",
        ).readText(),
      )

      managePomCaseMockServer.start()
      plpMockServer.start()
      sanMockServer.start()
      activitiesMockServer.start()
      prisonerBaseLocationMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMockServers() {
      nDeliusMockServer.stop()
      hmppsAuthMockServer.stop()
      prisonerOffenderSearchMockServer.stop()
      managePomCaseMockServer.stop()
      plpMockServer.stop()
      sanMockServer.stop()
      activitiesMockServer.stop()
      prisonerBaseLocationMockServer.stop()
      corePersonRecordMockServer.stop()
    }
  }

  fun setToLao(
    userExcluded: Boolean = true,
    userRestricted: Boolean = true,
  ) {
    nDeliusMockServer.stubForPost(
      "/probation-cases/access",
      """
          {
            "crns": ["${Companion.crn}"]
          }
          """.removeWhitespaceAndNewlines(),
      """
      {
        "access": [{
          "crn": "${Companion.crn}",
          "userExcluded": $userExcluded,
          "userRestricted": $userRestricted
        }]
      }
      """.trimIndent(),
    )
  }

  fun getAuthHeader(
    cn: String = defaultCn,
    serialNumber: String? = null,
  ): HttpHeaders {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=$cn")
    headers.set("cert-serial-number", serialNumber ?: certSerialNumber)
    return headers
  }

  fun getExpectedResponse(filename: String): String = File("./src/test/resources/expected-responses/$filename").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

  fun callApi(path: String): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader()))

  fun callApiWithCN(
    path: String,
    cn: String,
    serialNumber: String? = null,
  ): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader(cn, serialNumber)))

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

  fun putApi(path: String): ResultActions =
    mockMvc.perform(
      put(path)
        .headers(getAuthHeader()),
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

  fun getTestQueue(queue: String): TestQueue {
    val queue = queueProvider.findByQueueId(queue)
    when (queue) {
      is TestQueue -> return queue
      else -> throw IllegalStateException("TestQueue $queue not found")
    }
  }

  fun queueMessageCount(queue: String) = getTestQueue(queue).messageCount()

  fun checkQueueIsEmpty(queue: String) = queueMessageCount(queue) == 0

  fun lastQueueMessage(queue: String) = getTestQueue(queue).lastMessage()
}
