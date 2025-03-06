package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetVisitBalancesForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec({
    val nomisApiMockServer = ApiMockServer.create(UpstreamApi.NOMIS)
    val offenderNumber = "A7777ZZ"
    val visitBalancesPath = "/api/bookings/offenderNo/$offenderNumber/visit/balances"

    beforeEach {
      nomisApiMockServer.start()
      nomisApiMockServer.stubForGet(
        visitBalancesPath,
        """
        {
          "remainingVo": 1073741824,
          "remainingPvo": 1073741824,
          "latestIepAdjustDate": "2025-03-04",
          "latestPrivIepAdjustDate": "2025-03-04"
        }
        """.removeWhitespaceAndNewlines(),
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("NOMIS")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nomisApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      nomisGateway.getVisitBalances(offenderNumber)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
    }

    it("returns visit balances for the matching offender number") {
      val response = nomisGateway.getVisitBalances(offenderNumber)

      response.data.shouldNotBeNull()
      response.data?.remainingVo.shouldBe(1073741824)
    }

    it("returns an error when 400 Bad Request is returned because of an invalid request") {
      nomisApiMockServer.stubForGet(visitBalancesPath, "", HttpStatus.BAD_REQUEST)

      val response = nomisGateway.getVisitBalances(offenderNumber)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.NOMIS)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
    }

    it("returns an error when 404 Not Found is returned because no person is found") {
      nomisApiMockServer.stubForGet(visitBalancesPath, "", HttpStatus.NOT_FOUND)

      val response = nomisGateway.getVisitBalances(offenderNumber)

      response.errors.shouldHaveSize(1)
      response.errors
        .first()
        .causedBy
        .shouldBe(UpstreamApi.NOMIS)
      response.errors
        .first()
        .type
        .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
    }
  })
