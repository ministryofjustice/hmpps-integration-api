package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagTestConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway

@ActiveProfiles("test")
@ContextConfiguration(
  classes = [NDeliusGateway::class],
)
@Import(FeatureFlagTestConfig::class)
class MetaDataTest(
  val deliusGateway: NDeliusGateway,
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec(
    {
      describe("MetaData") {
        it("should return valid metadata") {
          assertNotNull(deliusGateway.metaData())
        }
      }
    },
  )
