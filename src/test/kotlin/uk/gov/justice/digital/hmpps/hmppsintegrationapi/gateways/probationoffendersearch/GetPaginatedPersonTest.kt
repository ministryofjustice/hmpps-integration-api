package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.RestClientConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationAccessGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsAuthUsernames
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.oboconfig.OboUser
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PersonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.REDACTION_MASKING_TEXT
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ManageUsersService
import java.io.File
import kotlin.collections.emptyList

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class, ProbationAccessGateway::class, ManageUsersService::class, ManageUsersGateway::class],
)
@Import(RestClientConfig::class)
class GetPaginatedPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec(
    {
      val probationOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PROBATION_OFFENDER_SEARCH)
      val probationAccessMockServer = ApiMockServer.create(UpstreamApi.PROBATION_ACCESS)

      val requestContext = buildRequestContext("testUser")

      val searchRequest =
        PersonSearchRequest(
          "John",
          surname = "Smith",
          dateOfBirth = "1996-02-10",
        )
      val path = searchRequest.uriString(1, 10)

      val deliusUser = "DELIUS_USER"
      val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = deliusUser))
      val requestContextLao = buildRequestContext("testUser", oboUser = oboUser)
      val searchRequestLao =
        PersonSearchRequest(
          "Harry",
          surname = "Jones",
          dateOfBirth = "1996-02-10",
        )
      val pathLao = searchRequestLao.uriString(1, 10)

      beforeEach {

        whenever(featureFlag.isEnabled(FeatureFlagConfig.OBO_DELIUS_USER_LAO_CHECK)).thenReturn(true)

        probationOffenderSearchMockServer.start()
        if (!probationAccessMockServer.isRunning()) {
          probationAccessMockServer.start()
        }
        probationOffenderSearchMockServer.stubForPost(
          path,
          defaultObjectMapper.writeValueAsString(searchRequest.toMap()),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/PaginatedOffenderSearchResponse.json").readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("Probation Offender Search", requestContext)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )

        whenever(hmppsAuthGateway.getClientToken("Probation Offender Search", requestContextLao)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
        whenever(hmppsAuthGateway.getClientToken("PROBATION_ACCESS", null)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )

        probationAccessMockServer.stubForPost(
          "/user/$deliusUser/access",
          defaultObjectMapper.writeValueAsString(listOf("A888888", "A777777", "A666666")),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponse.json").readText(),
        )

        probationOffenderSearchMockServer.stubForPost(
          pathLao,
          defaultObjectMapper.writeValueAsString(searchRequestLao.toMap()),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/MultipleOffenderSearchResponse.json").readText(),
        )
      }

      afterTest {
        probationOffenderSearchMockServer.stop()
        probationAccessMockServer.stop()
      }

      describe("POST /search/people returns a response") {
        it("returns a search response") {
          val personSearchResponse = probationOffenderSearchGateway.personSearch(searchRequest, PaginatedRequest(1, 10), requestContext)
          assertThat(personSearchResponse).isNotNull
        }
      }

      describe("User LAO redaction") {
        it("redacts personal details where required") {
          val personSearchResponse =
            probationOffenderSearchGateway.personSearch(
              searchRequestLao,
              PaginatedRequest(1, 10),
              requestContextLao,
            )
          assertThat(personSearchResponse.data[1].firstName).isEqualTo(REDACTION_MASKING_TEXT)
        }
        it("does not redact personal details where required when feature flag is disabled") {
          whenever(featureFlag.isEnabled(FeatureFlagConfig.OBO_DELIUS_USER_LAO_CHECK)).thenReturn(false)
          val personSearchResponse =
            probationOffenderSearchGateway.personSearch(
              searchRequestLao,
              PaginatedRequest(1, 10),
              requestContextLao,
            )
          assertThat(personSearchResponse.data[1].firstName).isNotEqualTo(REDACTION_MASKING_TEXT)
        }

        it("returns an empty list when a user does not have a delius account") {
          val oboUser = OboUser("testUser", authUsernames = HmppsAuthUsernames(deliusUsername = null))
          val requestContext = buildRequestContext("testUser", oboUser = oboUser)
          whenever(featureFlag.isEnabled(FeatureFlagConfig.OBO_DELIUS_USER_LAO_CHECK)).thenReturn(false)
          val personSearchResponse =
            probationOffenderSearchGateway.personSearch(
              searchRequestLao,
              PaginatedRequest(1, 10),
              requestContext,
            )
          assertThat(personSearchResponse.data).isEqualTo(emptyList<Person>())
        }
      }
    },
  )
