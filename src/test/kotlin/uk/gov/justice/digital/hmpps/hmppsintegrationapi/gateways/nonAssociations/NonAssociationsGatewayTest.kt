package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nonAssociations

import io.kotest.core.spec.style.DescribeSpec
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NonAssociationsGateway

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NonAssociationsGateway::class],
)
class NonAssociationsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nonAssociationsGateway: NonAssociationsGateway,
) : DescribeSpec({
    it("authenticates using HMPPS Auth with credentials") {
      nonAssociationsGateway.getNonAssociationsForPerson()

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NON-ASSOCIATIONS")
    }
  })
