package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.AuthorisationConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.CacheDisabledTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.AuthorisationService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestConstants.DEFAULT_CRN
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestPropertySource(properties = ["cache-enabled=false"])
@Import(CacheDisabledTestConfiguration::class)
abstract class IntegrationTestBase {
  @MockitoSpyBean
  lateinit var featureFlagConfig: FeatureFlagConfig

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
  lateinit var authorisationService: AuthorisationService

  @MockitoSpyBean
  lateinit var authorisationConfig: AuthorisationConfig

  @MockitoSpyBean
  lateinit var eventNotificationRepository: JdbcTemplateEventNotificationRepository

  @MockitoSpyBean
  lateinit var authGateway: HmppsAuthGateway

  @MockitoSpyBean
  lateinit var manageUsersGateway: ManageUsersGateway

  @Autowired
  lateinit var mockMvc: MockMvc

  lateinit var authSpy: WebClientWrapper

  @BeforeEach
  fun evictAllCaches() {
    reset(telemetryService)
    reset(activitiesGateway)
    reset(prisonerOffenderSearchGateway)
    reset(corePersonRecordGateway)
    reset(nDeliusGateway)
    reset(featureFlagConfig)
    reset(authorisationService)
    reset(eventNotificationRepository)
    reset(authGateway)

    // Create a spy on the auth client in order to check
    val authClient =
      WebClientWrapper(
        "http://localhost:3000",
      )
    authSpy = spy(authClient)
    ReflectionTestUtils.setField(authGateway, "webClientWrapper", authSpy)

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

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdActiveInPrison",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/ActivePrisonerByIdResponse.json",
      ).readText(),
    )

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdNotActiveInPrison",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/InactivePrisonerByIdResponse.json",
      ).readText(),
    )

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdNotActiveInPrisonOrProb",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/NoStatusPrisonerByIdResponse.json",
      ).readText(),
    )

    prisonerOffenderSearchMockServer.stubForGet(
      "/prison/$prisonId/prisoners?page=0&size=10",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/GetPersons.json",
      ).readText(),
    )

    prisonerOffenderSearchMockServer.stubForGet(
      "/prison/$emptyPrisonId/prisoners?page=0&size=10",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/GetPersonsEmpty.json",
      ).readText(),
      HttpStatus.NOT_FOUND,
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
  final val nomsIdFromProbation = "G5555TT"

  companion object {
    private val nomsId = "G2996UX"
    private val nomsIdFromProbation = "G5555TT"
    private val crn = DEFAULT_CRN

    val nomsIdActiveInPrison = "A3646EA"
    val nomsIdNotActiveInPrison = "A3646EB"
    val nomsIdNotActiveInPrisonOrProb = "A3646EC"
    val prisonId = "MKI"
    val emptyPrisonId = "MKD"
    val cellKey = "MKI-A-1-001"

    val crnActiveInProbation = "A654321"
    val crnNotActiveInProbation = "A765432"
    val crnNotActiveInPrisonOrProb = "A876543"
    val crnUnknownInPrison = "A987654"

    val clientReference = "AABDC234"
    val visitReference = "123456"
    val contactId = 123456L

    val certSerialNumber = "9572494320151578633330348943480876283449388176"
    val revokedSerialNumber = "8472494320151578633330348943480876283449388195"

    val imageByteArray = byteArrayOf(0x48, 101, 108, 108, 111)

    val dateNow = LocalDate.now().toString()
    val dateTimeNow = LocalDateTime.now().toString()

    val gatewaysFolder = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways"
    private val hmppsAuthMockServer = HmppsAuthMockServer()
    val prisonerOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PRISONER_OFFENDER_SEARCH)
    val managePomCaseMockServer = ApiMockServer.create(UpstreamApi.MANAGE_POM_CASE)
    val plpMockServer = ApiMockServer.create(UpstreamApi.PLP)
    val sanMockServer = ApiMockServer.create(UpstreamApi.SAN)
    val activitiesMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
    val nDeliusMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)
    val prisonerBaseLocationMockServer = ApiMockServer.create(UpstreamApi.PRISONER_BASE_LOCATION)
    val corePersonRecordMockServer = ApiMockServer.create(UpstreamApi.CORE_PERSON_RECORD)
    val arnsMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)
    val probationSearchMockServer = ApiMockServer.create(UpstreamApi.PROBATION_OFFENDER_SEARCH)
    val manageUsersMockServer = ApiMockServer.create(UpstreamApi.MANAGE_USERS)
    val remandAndSentencingMockServer = ApiMockServer.create(UpstreamApi.REMAND_AND_SENTENCING)
    val courtRegisterMockServer = ApiMockServer.create(UpstreamApi.COURT_REGISTER)
    val locationsInsidePrisonServer = ApiMockServer.create(UpstreamApi.LOCATIONS_INSIDE_PRISON)
    val alertsServer = ApiMockServer.create(UpstreamApi.PRISONER_ALERTS)
    val incentivesServer = ApiMockServer.create(UpstreamApi.INCENTIVES)
    val managePrisonVisitsServer = ApiMockServer.create(UpstreamApi.MANAGE_PRISON_VISITS)
    val personalRelationshipServer = ApiMockServer.create(UpstreamApi.PERSONAL_RELATIONSHIPS)
    val nonAssociationServer = ApiMockServer.create(UpstreamApi.NON_ASSOCIATIONS)
    val prisonApiServer = ApiMockServer.create(UpstreamApi.PRISON_API)
    val createAndVaryLicenceServer = ApiMockServer.create(UpstreamApi.CVL)
    val caseNotesServer = ApiMockServer.create(UpstreamApi.CASE_NOTES)
    val assesmentRisksAndNeedsServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

    @BeforeEach
    fun setUp() {
    }

    @BeforeAll
    @JvmStatic
    fun startMockServers() {
      hmppsAuthMockServer.start()
      corePersonRecordMockServer.start()
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", HmppsAuthMockServer.TOKEN)
      hmppsAuthMockServer.stubGetOAuthToken("client", "client-secret", HmppsAuthMockServer.TOKEN, "testName")

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
        writeAsJson(mapOf("nomsNumber" to "$nomsIdNotActiveInPrison")),
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

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("crn" to crnActiveInProbation)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseStatusActive.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("crn" to crnNotActiveInProbation)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseStatusInactive.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("crn" to crnNotActiveInPrisonOrProb)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseStatusNone.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("crn" to crnUnknownInPrison)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseUnknownPrisonId.json",
        ).readText(),
      )

      nDeliusMockServer.stubForPost(
        "/search/probation-cases",
        writeAsJson(mapOf("nomsNumber" to nomsIdNotActiveInPrisonOrProb)),
        File(
          "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseStatusNone.json",
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

      nDeliusMockServer.stubForGet(
        "/identifier-converter/noms-to-crn/$nomsId",
        """
                  {
                  "crn": "$crn",
                  "nomsId": "$nomsId"
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/exists-in-delius/crn/$crn",
        """
                  {
                  "crn": "$crn",
                  "existsInDelius": true
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/exists-in-delius/crn/$crnNotActiveInProbation",
        """
                  {
                  "crn": "$crnActiveInProbation",
                  "existsInDelius": true
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/exists-in-delius/crn/$crnActiveInProbation",
        """
              {
                  "crn": "$crnNotActiveInProbation",
                  "existsInDelius": true
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/exists-in-delius/crn/$crnUnknownInPrison",
        """
              {
                  "crn": "$crnUnknownInPrison",
                  "existsInDelius": true
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/exists-in-delius/crn/$crnNotActiveInPrisonOrProb",
        """
              {
                  "crn": "$crnNotActiveInPrisonOrProb",
                  "existsInDelius": true
              }
              """,
      )

      nDeliusMockServer.stubForGet(
        "/case-details/$crn/1234",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/CaseDetails.json",
        ).readText(),
      )

      manageUsersMockServer.start()
      manageUsersMockServer.stubForGet(
        "/users/search?username=testName&authSources=azuread",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundResponse.json",
        ).readText(),
      )
      manageUsersMockServer.stubForGet(
        "/prisonusers/by-email/testName/details",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponse.json",
        ).readText(),
      )
      manageUsersMockServer.stubForGet(
        "/prisonusers/TEST_USER/caseloads",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUserCaseloadResponse.json",
        ).readText(),
      )
      manageUsersMockServer.stubForGet(
        "/prisonusers/TEST2_USER/caseloads",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUserCaseloadResponseUser2.json",
        ).readText(),
      )

      probationSearchMockServer.start()
      managePomCaseMockServer.start()
      plpMockServer.start()
      sanMockServer.start()
      activitiesMockServer.start()
      prisonerBaseLocationMockServer.start()
      remandAndSentencingMockServer.start()
      remandAndSentencingMockServer.stubForGet(
        "/person/$nomsId/sentenced-court-cases",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/remandAndSentencing/fixtures/SentencedCourtCasesResponse.json",
        ).readText(),
      )
      courtRegisterMockServer.start()
      courtRegisterMockServer.stubForGet(
        "/courts/id/ACCRYC",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/courtregister/fixtures/GetCourtResponse.json",
        ).readText(),
      )
      locationsInsidePrisonServer.start()
      locationsInsidePrisonServer.stubForGet(
        "/locations/key/$cellKey",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/locationsInsidePrison/fixtures/CellLocation.json",
        ).readText(),
      )

      locationsInsidePrisonServer.stubForGet(
        "/locations/residential-summary/$prisonId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/locationsInsidePrison/fixtures/ResidentialSummary.json",
        ).readText(),
      )

      locationsInsidePrisonServer.stubForGet(
        "/locations/residential-summary/$prisonId?parentPathHierarchy=A",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/locationsInsidePrison/fixtures/ResidentialSummary.json",
        ).readText(),
      )

      locationsInsidePrisonServer.stubForGet(
        "/locations/prison/$prisonId/residential-hierarchy",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/locationsInsidePrison/fixtures/ResidentialHierarchy.json",
        ).readText(),
      )

      alertsServer.start()

      alertsServer.stubForGet(
        "/prisoners/$nomsId/alerts?page=0&size=10",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonerAlerts/fixtures/PrisonerAlerts.json",
        ).readText(),
      )

      alertsServer.stubForGet(
        "/prisoners/$nomsId/alerts?page=0&size=10&isActive=true",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonerAlerts/fixtures/PrisonerAlerts.json",
        ).readText(),
      )

      alertsServer.stubForGet(
        "/prisoners/$nomsId/alerts?page=0&size=10&alertCode=HA,HA2,XA,XC,XCA,XCI,XCO,XCOL,XCOP,XCOR,XEL,XELH,XER,XHT,XILLENT,XIS,XRF",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonerAlerts/fixtures/PrisonerAlerts.json",
        ).readText(),
      )

      incentivesServer.start()

      incentivesServer.stubForGet(
        "/incentive-reviews/prisoner/$nomsId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/incentives/fixtures/IncentiveReviews.json",
        ).readText(),
      )

      managePrisonVisitsServer.start()

      managePrisonVisitsServer.stubForGet(
        "/visits/$visitReference",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonVisits/fixtures/Visit.json",
        ).readText(),
      )

      managePrisonVisitsServer.stubForGet(
        "/visits/external-system/$clientReference",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonVisits/fixtures/ExternalSystemVisit.json",
        ).readText(),
      )

      managePrisonVisitsServer.stubForGet(
        "/visits/search?prisonId=$prisonId&visitStatus=BOOKED&page=0&size=10&visitStartDate=2024-01-01&visitEndDate=2024-01-14",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonVisits/fixtures/VisitSearch.json",
        ).readText(),
      )

      managePrisonVisitsServer.stubForGet(
        "/visits/search/future/$nomsId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisonVisits/fixtures/VisitFuture.json",
        ).readText(),
      )

      personalRelationshipServer.start()

      personalRelationshipServer.stubForGet(
        "/contact/$contactId/linked-prisoners",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/LinkedPrisoners.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/contact/$contactId/linked-prisoners?page=0&size=10",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/LinkedPrisoners.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/prisoner-contact/$contactId/restriction",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/PrisonerContactRestriction.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/prisoner/$nomsId/contact?page=0&size=10",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/PrisonerContactSearch.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/prisoner/$nomsId/contact?page=0&size=10&emergencyContactOrNextOfKin=true",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/PrisonerContactSearch.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/prisoner/$nomsId/number-of-children",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/NumberOfChildren.json",
        ).readText(),
      )

      personalRelationshipServer.stubForGet(
        "/contact/$contactId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/personalRelationships/fixtures/ContactByContactId.json",
        ).readText(),
      )

      nonAssociationServer.start()

      nonAssociationServer.stubForGet(
        "/prisoner/$nomsId/non-associations?includeOpen=true&includeClosed=false",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nonAssociations/fixtures/PrisonerNonAssociations.json",
        ).readText(),
      )

      prisonApiServer.start()

      prisonApiServer.stubForGet(
        "/api/images/2461788/data",
        imageByteArray.toString(),
      )

      prisonApiServer.stubForGet(
        "/api/images/offenders/$nomsId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/OffendersImagesMetadata.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/transactions/prison/$prisonId/offenders/$nomsId/accounts/spends?from_date=$dateNow&to_date=$dateNow",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonTransactions.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/transactions/prison/$prisonId/offenders/$nomsId/accounts/spends?from_date=2024-01-01&to_date=2024-01-14",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonTransactions.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/v1/prison/$prisonId/offenders/$nomsId/transactions/$clientReference",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonTransaction.json",
        ).readText(),
      )

      prisonApiServer.stubForPost(
        "/api/v1/prison/$prisonId/offenders/$nomsId/transactions",
        writeAsJson(
          mapOf(
            "type" to "CANT",
            "description" to "Canteen Purchase of £16.34",
            "amount" to 1634,
            "client_transaction_id" to "CL123212",
            "client_unique_ref" to "CLIENT121131-0_11",
          ),
        ),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PostPersonTransaction.json",
        ).readText(),
      )

      prisonApiServer.stubForPost(
        "/api/finance/prison/$prisonId/offenders/$nomsId/transfer-to-savings",
        writeAsJson(
          mapOf(
            "description" to "Canteen Purchase of £16.34",
            "amount" to 1634,
            "client_transaction_id" to "CL123212",
            "client_unique_ref" to "CLIENT121131-0_11",
          ),
        ),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PostPersonTransfer.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId/addresses",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonAddresses.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/movements/offender/$nomsId?movementTypes=TRN&movementTypes=CRT&allBookings=true",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonMovements.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/movements/offender/$nomsIdFromProbation?movementTypes=TRN&movementTypes=CRT&allBookings=true",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonMovements.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/bookings/offenderNo/$nomsId/offenceHistory",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonOffences.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId/prison-timeline",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonTimeline.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/bookings/0001200924/reasonable-adjustments?",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonReasonableAdjustments.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonCategories.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offender-sentences?offenderNo=$crn",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonOffenderSentences.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId/sentences",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonSentences.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId/booking/latest/sentence-summary",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonSentenceSummary.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/bookings/offenderNo/$nomsId/visit/balances",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonVisitBalances.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/offenders/$nomsId/offender-restrictions",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonRestrictions.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/v1/prison/$prisonId/offenders/$nomsId/accounts",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonAccounts.json",
        ).readText(),
      )

      prisonApiServer.stubForGet(
        "/api/v1/prison/$nomsId/live_roll",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/nomis/fixtures/PersonLiveRoll.json",
        ).readText(),
      )

      createAndVaryLicenceServer.start()

      createAndVaryLicenceServer.stubForGet(
        "/public/licences/id/99999",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/createAndVaryLicence/fixtures/PersonLicences.json",
        ).readText(),
      )

      createAndVaryLicenceServer.stubForGet(
        "/public/licence-summaries/crn/$crn",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/createAndVaryLicence/fixtures/PersonLicenceSummaries.json",
        ).readText(),
      )

      caseNotesServer.start()

      caseNotesServer.stubForPost(
        "/search/case-notes/$nomsId",
        """{
          "includeSensitive" : true,
          "occurredFrom" : "${dateTimeNow}Z",
          "occurredTo" : "${dateTimeNow}Z",
          "page" : 1,
          "size" : 10
        }""".removeWhitespaceAndNewlines(),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/caseNotes/fixtures/PersonCaseNotes.json",
        ).readText(),
      )

      caseNotesServer.stubForPost(
        "/search/case-notes/$nomsId",
        """{
          "includeSensitive" : true,
          "page" : 1,
          "size" : 10
        }""".removeWhitespaceAndNewlines(),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/caseNotes/fixtures/PersonCaseNotes.json",
        ).readText(),
      )

      caseNotesServer.stubForPost(
        "/search/case-notes/$nomsId",
        """{
          "includeSensitive" : true,
          "typeSubTypes" : [ {
            "type" : "CAB",
            "subTypes" : [ ]
          } ],
          "page" : 1,
          "size" : 10
          }""".removeWhitespaceAndNewlines(),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/caseNotes/fixtures/PersonCaseNotes.json",
        ).readText(),
      )

      assesmentRisksAndNeedsServer.start()
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
      probationSearchMockServer.stop()
      manageUsersMockServer.stop()
      remandAndSentencingMockServer.stop()
      locationsInsidePrisonServer.stop()
      alertsServer.stop()
      incentivesServer.stop()
      managePrisonVisitsServer.stop()
      personalRelationshipServer.stop()
      nonAssociationServer.stop()
      prisonApiServer.stop()
      createAndVaryLicenceServer.stop()
      caseNotesServer.stop()
      assesmentRisksAndNeedsServer.stop()
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
    oboValue: String? = null,
  ): HttpHeaders {
    val headers = HttpHeaders()
    headers.set("subject-distinguished-name", "C=GB,ST=London,L=London,O=Home Office,CN=$cn")
    headers.set("cert-serial-number", serialNumber ?: certSerialNumber)
    if (oboValue != null) {
      headers.set("X-On-Behalf-Of", oboValue)
    }
    return headers
  }

  fun getExpectedResponse(filename: String): String = File("./src/test/resources/expected-responses/$filename").readText(Charsets.UTF_8).removeWhitespaceAndNewlines()

  fun callApi(path: String): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader()))

  fun callApiWithCN(
    path: String,
    cn: String,
    serialNumber: String? = null,
    oboValue: String? = null,
  ): ResultActions = mockMvc.perform(get(path).headers(getAuthHeader(cn, serialNumber, oboValue)))

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
}
