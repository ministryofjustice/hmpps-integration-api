package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.EPF_ENDPOINT_INCLUDES_LAO
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagTestConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateCaseDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class],
)
@Import(FeatureFlagTestConfig::class)
class GetEPFPersonDetailsTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
  @MockitoSpyBean val featureFlagSpy: FeatureFlagConfig,
  val deliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val nDeliusApiMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)
      val hmppsId = "X777776"
      val eventNumber = 1234
      val path = "/case-details/$hmppsId/$eventNumber"

      beforeEach {
        nDeliusApiMockServer.start()
        nDeliusApiMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/GetEPFPersonDetailsResponse.json",
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
        deliusGateway.getEpfCaseDetailForPerson(hmppsId, eventNumber)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("nDelius")
      }

      it("returns case detail for the matching CRN") {
        val response = deliusGateway.getEpfCaseDetailForPerson(hmppsId, eventNumber)

        response.data.shouldBe(
          generateCaseDetail(),
        )
      }

      it("returns limited access information when the feature flag is enabled") {
        whenever(featureFlagSpy.isEnabled(EPF_ENDPOINT_INCLUDES_LAO)).thenReturn(true)
        val response = deliusGateway.getEpfCaseDetailForPerson(hmppsId, eventNumber)

        response.data.shouldBe(
          generateCaseDetail(includeLimitedAccess = true),
        )
      }
    },
  )
