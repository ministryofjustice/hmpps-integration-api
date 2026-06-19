package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.probationoffendersearch

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AddressSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.dsl.objectMapper
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ProbationOffenderSearchGateway::class],
)
class PostAddressesSearchTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) : DescribeSpec(
    {
      val probationOffenderSearchMockServer = ApiMockServer.create(UpstreamApi.PROBATION_OFFENDER_SEARCH)
      val requestContext = buildRequestContext()

      val requestBody =
        AddressSearchRequest(
          buildingName = "Burnham House",
          addressNumber = "1",
          streetName = "Church Road",
          postcode = "LM2 1BF",
        )

      beforeEach {

        probationOffenderSearchMockServer.start()

        val fixturesPath = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/address-search-response.json"
        probationOffenderSearchMockServer.stubForPost(
          "/search/addresses",
          resBody =
            File(
              fixturesPath,
            ).readText(),
          reqBody = objectMapper.writeValueAsString(requestBody),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("Probation Offender Search", requestContext)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        probationOffenderSearchMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        probationOffenderSearchGateway.addressSearch(requestBody, requestContext)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search", requestContext)
      }

      describe("GET /search/addresses returns a response") {
        it("returns a search response") {

          val addressResponse = probationOffenderSearchGateway.addressSearch(requestBody, buildRequestContext())
          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Probation Offender Search", requestContext)
          addressResponse.data
            ?.personAddresses
            ?.size
            .shouldBe(1)
          addressResponse.data
            ?.personAddresses[0]
            ?.address
            ?.streetName
            .shouldBe("Church Road")
        }

        it("returns a 404 response") {
          probationOffenderSearchMockServer.stubForPost(
            "/search/addresses",
            resBody = "",
            reqBody = objectMapper.writeValueAsString(requestBody),
            status = HttpStatus.NOT_FOUND,
          )
          val addressResponse = probationOffenderSearchGateway.addressSearch(requestBody, buildRequestContext())
          addressResponse.errors.size.shouldBe(1)
          addressResponse.errors[0].shouldBe(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH))
        }

        it("returns a 400 response") {
          probationOffenderSearchMockServer.stubForPost(
            "/search/addresses",
            resBody = "",
            reqBody = objectMapper.writeValueAsString(requestBody),
            status = HttpStatus.BAD_REQUEST,
          )
          val addressResponse = probationOffenderSearchGateway.addressSearch(requestBody, buildRequestContext())
          addressResponse.errors.size.shouldBe(1)
          addressResponse.errors[0].shouldBe(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, causedBy = UpstreamApi.PERSONAL_RELATIONSHIPS))
        }
      }
    },
  )
