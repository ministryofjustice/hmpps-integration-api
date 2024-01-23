package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.createAndVaryLicence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CreateAndVaryLicenceGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.CreateAndVaryLicenceApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [CreateAndVaryLicenceGateway::class],
)
class GetLicenceConditionsTests(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  private val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
) : DescribeSpec(
  {
    val createAndVaryLicenceApiMockServer = CreateAndVaryLicenceApiMockServer()
    val conditionId = "X777776"

    beforeEach {
      createAndVaryLicenceApiMockServer.start()
      createAndVaryLicenceApiMockServer.stubGetLicenceConditions(
        conditionId,
        """
          {
          "conditions":
            {
              "AP":
                {
                  "standard":
                  [
                    {
                      "text": "Not commit any offence"
                    }
                  ]
                }
            }
          }
        """,
      )

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("CVL")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      createAndVaryLicenceApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      createAndVaryLicenceGateway.getLicenceConditions(conditionId)

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("CVL")
    }

    it("returns licence condition for the matching ID") {
      val response = createAndVaryLicenceGateway.getLicenceConditions(conditionId)

      response.data?.first()?.condition.shouldBe("Not commit any offence")
    }

    it("returns an error when 404 NOT FOUND is returned") {
      createAndVaryLicenceApiMockServer.stubGetLicenceConditions(
        conditionId,
        """
        [{
          "developerMessage": "cannot find person"
        }]
        """,
        HttpStatus.NOT_FOUND,
      )

      val response = createAndVaryLicenceGateway.getLicenceConditions(conditionId)

      response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
    }
  },
)
