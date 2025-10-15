package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationintegrationepf.LimitedAccess
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
class GetAccessLimitations(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  val deliusGateway: NDeliusGateway,
) : DescribeSpec(
  {
    val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)
    val hmppsId = "X150876"
    val path = "/case/$hmppsId/access-limitations"

    beforeEach {
      nDeliusApiMockServer.start()
      nDeliusApiMockServer.stubForGet(
        path,
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetAccessLimitationsResponse.json",
        ).readText(),
      )

      Mockito.reset(hmppsAuthGateway)
      Mockito.reset(featureFlagConfig)
      whenever(hmppsAuthGateway.getClientToken("nDelius")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      nDeliusApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      deliusGateway.getAccessLimitations(hmppsId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
    }

    it("returns limited access details for the matching CRN") {
      val response = deliusGateway.getAccessLimitations(hmppsId)

      response.data.shouldBe(
        LimitedAccess(
          excludedFrom = listOf(),
          exclusionMessage = null,
          restrictedTo = listOf(LimitedAccess.AccessLimitation("someone@justice.gov.uk")),
          restrictionMessage = "This case is restricted. Please contact someone for more information."),
      )
    }
  },
)
