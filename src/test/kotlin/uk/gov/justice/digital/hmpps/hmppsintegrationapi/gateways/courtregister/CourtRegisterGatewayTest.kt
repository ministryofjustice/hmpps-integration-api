package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.courtregister

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CourtRegisterGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File

internal const val FIXTURES_DIR = "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/courtregister/fixtures"

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CourtRegisterGateway::class],
)
class CourtRegisterGatewayTest(
  @MockitoBean private val hmppsAuthGateway: HmppsAuthGateway,
  private val courtRegisterGateway: CourtRegisterGateway,
) : DescribeSpec(
    {

      describe("Court Register Gateway") {
        fun readFixtures(fileName: String): String = File("$FIXTURES_DIR/$fileName").readText()
        val knownCourtId = "ACCRYC"
        val unknownCourtId = "ACCRYY"

        describe("when court register api is enabled") {
          val courtRegisterApiMockServer = ApiMockServer.create(UpstreamApi.COURT_REGISTER)
          val knownPrisonerResponse = readFixtures("GetCourtResponse.json")
          val requestContext = buildRequestContext("TestUser")

          beforeTest {
            courtRegisterApiMockServer.start()
          }

          beforeEach {
            whenever(hmppsAuthGateway.getClientToken("COURT_REGISTER", requestContext)).thenReturn(HmppsAuthMockServer.TOKEN)

            with(courtRegisterApiMockServer) {
              stubForGet(
                path = "/courts/id/$unknownCourtId",
                status = HttpStatus.NOT_FOUND,
                body = "",
              )

              stubForGet(
                path = "/courts/id/$knownCourtId",
                body = knownPrisonerResponse,
              )
            }
          }

          afterTest {
            courtRegisterApiMockServer.stop()
            courtRegisterApiMockServer.resetValidator()
          }

          describe("#getCourt()") {
            it("does not return court for unknown court id") {
              val response = courtRegisterGateway.getCourt(unknownCourtId, requestContext)

              response.data shouldBe null
              response.errors.firstOrNull().shouldNotBeNull().let {
                it.causedBy shouldBe UpstreamApi.COURT_REGISTER
                it.type shouldBe UpstreamApiError.Type.ENTITY_NOT_FOUND
              }
            }

            it("returns court for known court id") {
              val response = courtRegisterGateway.getCourt(knownCourtId, requestContext)

              response.errors.shouldBeEmpty()
              response.data.shouldNotBeNull().let {
                it.courtId shouldBe "ACCRYC"
                it.courtName shouldBe "Accrington Youth Court"
                it.courtDescription shouldBe "Accrington Youth Court"
                it.active shouldBe true
                it.type shouldBe CourtType("COU", "County Court/County Divorce Ct")
              }

              courtRegisterApiMockServer.assertValidationPassed()
            }
          }
        }
      }
    },
  )
