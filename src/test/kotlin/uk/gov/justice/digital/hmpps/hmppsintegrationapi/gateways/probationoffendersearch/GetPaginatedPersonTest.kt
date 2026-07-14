package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PersonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class],
)
class GetPaginatedPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlag: FeatureFlagConfig,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec(
    {
      val probationOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PROBATION_OFFENDER_SEARCH)

      val requestContext = buildRequestContext("testUser")

      val searchRequest =
        PersonSearchRequest(
          "John",
          surname = "Smith",
          dateOfBirth = "1996-02-10",
        )
      val path = searchRequest.uriString(1, 10)
      beforeEach {

        val request = RequestContext

        probationOffenderSearchMockServer.start()
        probationOffenderSearchMockServer.stubForPost(
          path,
          objectMapper.writeValueAsString(searchRequest.toMap()),
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/PaginatedOffenderSearchResponse.json").readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("Probation Offender Search", requestContext)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        probationOffenderSearchMockServer.stop()
      }

      describe("POST /search/people returns a response") {
        it("returns a search response") {
          val personSearchResponse = probationOffenderSearchGateway.personSearch(searchRequest, PaginatedRequest(1, 10), requestContext)
          assertThat(personSearchResponse).isNotNull
        }
      }
    },
  )
