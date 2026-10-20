package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.cpr

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import jakarta.validation.ValidationException
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RestClientConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.WebClientWrapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CorePersonRecordGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationAccessGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase.Companion.gatewaysFolder
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr.CorePersonRecordSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUsernames
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig.OboUser
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.REDACTION_MASKING_TEXT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ManageUsersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestConstants.DEFAULT_CRN
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CorePersonRecordGateway::class, ProbationAccessGateway::class, ManageUsersGateway::class, ManageUsersService::class],
)
@Import(RestClientConfig::class)
class CorePersonRecordGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  private val corePersonRecordGateway: CorePersonRecordGateway,
) : DescribeSpec(
    {
      val cprMockServer = ApiMockServer.create(UpstreamApi.CORE_PERSON_RECORD)
      val probationAccessMockServer = ApiMockServer.create(UpstreamApi.PROBATION_ACCESS)
      val crn = DEFAULT_CRN
      val nomsId = "G2996UX"

      val deliusUser = "DELIUS_USER"
      val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = deliusUser))
      val requestContextLao = buildRequestContext("testUser", oboUser = oboUser)
      val requestLao =
        jacksonObjectMapper().readValue(
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-request-lao.json",
          ).readText(),
          CorePersonRecordSearchRequest::class.java,
        )

      beforeEach {
        cprMockServer.start()
        if (!probationAccessMockServer.isRunning) {
          probationAccessMockServer.start()
        }
        Mockito.reset(hmppsAuthGateway)

        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.OBO_DELIUS_USER_LAO_CHECK)).thenReturn(true)

        whenever(hmppsAuthGateway.getClientToken("CORE_PERSON_RECORD")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
        whenever(hmppsAuthGateway.getClientToken(eq("CORE_PERSON_RECORD"), any<RequestContext>())).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
        whenever(hmppsAuthGateway.getClientToken("PROBATION_ACCESS", null)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )

        probationAccessMockServer.stubForPost(
          "/user/$deliusUser/access",
          defaultObjectMapper.writeValueAsString(listOf("A669783", "Z163457")),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponse.json").readText(),
        )

        cprMockServer.stubForPost(
          "/person/vetting/search",
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-request-lao.json",
          ).readText(),
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-response-new.json",
          ).readText(),
          HttpStatus.OK,
        )

        cprMockServer.stubForGet(
          "/person/probation/$crn",
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
          ).readText(),
          HttpStatus.OK,
        )
        cprMockServer.stubForGet(
          "/person/prison/$nomsId",
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-response.json",
          ).readText(),
          HttpStatus.OK,
        )
      }

      afterTest {
        cprMockServer.stop()
        probationAccessMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, nomsId)
        verify(hmppsAuthGateway, times(1))
          .getClientToken("CORE_PERSON_RECORD")
      }

      it("upstream API successfully returns a core person record error for /person/probation") {
        val response = corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
        response.getIdentifier(IdentifierType.NOMS).shouldBe(nomsId)
        cprMockServer.assertValidationPassed()
      }

      it("upstream API successfully returns a core person record error for /person/prison") {
        val response = corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, nomsId)
        response.getIdentifier(IdentifierType.CRN).shouldBe(crn)
        cprMockServer.assertValidationPassed()
      }

      it("upstream API returns a Not Found error, throw EntityNotFoundException") {
        cprMockServer.stubForGet("/person/probation/$crn", "", HttpStatus.NOT_FOUND)
        val response =
          shouldThrow<EntityNotFoundException> {
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
          }
        response.message.shouldContain("Could not find core person record at /person/probation/$crn")
      }

      it("upstream API returns a bad request error, throw IllegalArgumentException") {
        cprMockServer.stubForGet("/person/prison/$crn", "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<ValidationException> {
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.NOMS, crn)
          }
        response.message.shouldContain("Invalid request to core person record /person/prison/$crn")
      }

      it("upstream API returns core person record search results") {
        val requestContext = buildRequestContext()
        cprMockServer.stubForPost(
          "/person/vetting/search",
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-request.json",
          ).readText(),
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-response-new.json",
          ).readText(),
          HttpStatus.OK,
        )
        val request =
          jacksonObjectMapper().readValue(
            File(
              "$gatewaysFolder/cpr/fixtures/core-person-record-search-request.json",
            ).readText(),
            CorePersonRecordSearchRequest::class.java,
          )

        val response = corePersonRecordGateway.corePersonRecordSearch(request, requestContext)
        response.data
          ?.data[0]
          ?.results[0]
          ?.identifiers
          ?.crn
          .shouldBe("A123456")
      }

      it("upstream API returns a bad request for a core person record search") {
        val requestContext = buildRequestContext()
        val request =
          jacksonObjectMapper().readValue(
            File(
              "$gatewaysFolder/cpr/fixtures/core-person-record-search-invalid-request.json",
            ).readText(),
            CorePersonRecordSearchRequest::class.java,
          )

        cprMockServer.stubForPost(
          "/person/vetting/search",
          File(
            "$gatewaysFolder/cpr/fixtures/core-person-record-search-invalid-request.json",
          ).readText(),
          "",
          HttpStatus.BAD_REQUEST,
        )

        val response = corePersonRecordGateway.corePersonRecordSearch(request, requestContext)
        response.data shouldBe null
        response.errors.shouldContain(UpstreamApiError(causedBy = UpstreamApi.CORE_PERSON_RECORD, type = UpstreamApiError.Type.BAD_REQUEST))
      }

      it("redacts personal details where required") {
        val response = corePersonRecordGateway.corePersonRecordSearch(requestLao, requestContextLao)
        response.data
          ?.data[0]
          ?.results[1]
          ?.name
          ?.firstName
          .shouldBe(REDACTION_MASKING_TEXT)
      }

      it("does not redact personal details where required when feature is disabled") {
        whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.OBO_DELIUS_USER_LAO_CHECK)).thenReturn(false)

        val response = corePersonRecordGateway.corePersonRecordSearch(requestLao, requestContextLao)
        response.data
          ?.data[0]
          ?.results[1]
          ?.name
          ?.firstName
          .shouldNotBe(REDACTION_MASKING_TEXT)
      }

      it("does not return nomis records when user does not have nomis role") {
        val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = deliusUser), hasPrisonRole = false)
        val requestContextOboUserNoNomis = buildRequestContext("testUser", oboUser = oboUser)
        val response = corePersonRecordGateway.corePersonRecordSearch(requestLao, requestContextOboUserNoNomis)
        val distinctSources =
          response.data
            ?.data
            ?.flatMap { it.results.mapNotNull { item -> item.sourceSystem } }
            ?.distinct() ?: emptyList()
        distinctSources shouldNotContain "NOMIS"
        distinctSources shouldContain "DELIUS"
      }

      it("does not return delius records when user does not have a delius user account") {
        val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = null), hasPrisonRole = true)
        val requestContextOboUserNoNomis = buildRequestContext("testUser", oboUser = oboUser)
        val response = corePersonRecordGateway.corePersonRecordSearch(requestLao, requestContextOboUserNoNomis)
        val distinctSources =
          response.data
            ?.data
            ?.flatMap { it.results.mapNotNull { item -> item.sourceSystem } }
            ?.distinct() ?: emptyList()
        distinctSources shouldNotContain "DELIUS"
        distinctSources shouldContain "NOMIS"
      }

      it("does not return any nomis or delius records when user does not have a delius user account and no nomis role") {
        val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = null), hasPrisonRole = false)
        val requestContextOboUserNoAccess = buildRequestContext("testUser", oboUser = oboUser)
        val response = corePersonRecordGateway.corePersonRecordSearch(requestLao, requestContextOboUserNoAccess)
        val distinctSources =
          response.data
            ?.data
            ?.flatMap { it.results.mapNotNull { item -> item.sourceSystem } }
            ?.distinct() ?: emptyList()
        distinctSources shouldNotContain "DELIUS"
        distinctSources shouldNotContain "NOMIS"
      }

      it("upstream API returns an unexpected exception, throw RuntimeException") {
        val client = Mockito.mock(WebClientWrapper::class.java)
        val mockResponse = WebClientWrapper.WebClientWrapperResponse.Error(emptyList())
        whenever(client.getResponseBodySpec(any(), any(), any(), isNull())).thenThrow(
          WebClientResponseException(
            500,
            "Error",
            null,
            null,
            null,
          ),
        )
        whenever(client.getErrorType(any(), any(), any(), any())).thenReturn(mockResponse)
        ReflectionTestUtils.setField(corePersonRecordGateway, "webClient", client)
        val response =
          shouldThrow<RuntimeException> {
            corePersonRecordGateway.corePersonRecordFor(IdentifierType.CRN, crn)
          }
        response.message.shouldContain("Error retrieving core person record from /person/probation/$crn")
      }
    },
  )
