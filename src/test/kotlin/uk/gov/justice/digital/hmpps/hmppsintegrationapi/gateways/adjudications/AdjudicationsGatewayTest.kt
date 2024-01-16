package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.adjudications

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AdjudicationsGateway::class],
)
class AdjudicationsGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val adjudicationsGateway: AdjudicationsGateway,
) : DescribeSpec(
  {
    it("gateway returns 200 status code") {
      val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
      response.data.shouldBe(emptyList())
    }
  },
)
