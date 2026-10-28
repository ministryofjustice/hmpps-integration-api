package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceCondition
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusLicenceConditions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.NDeliusSentenceLicenceCondition
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NDeliusGateway::class, FeatureFlagConfig::class],
)
class GetLicenceCondionsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val nDeliusGateway: NDeliusGateway,
) : DescribeSpec(
    {
      val nDeliusMockServer = ApiMockServer.create(UpstreamApi.NDELIUS)
      val hmppsId = "X777776"
      val path = "/case/$hmppsId/licence-conditions"
      val requestContext = buildRequestContext("testUser")

      val expected =
        NDeliusLicenceConditions(
          sentences =
            listOf(
              NDeliusSentenceLicenceCondition(
                eventNumber = 3,
                licenceConditions =
                  listOf(
                    NDeliusLicenceCondition(
                      mainCategory = NDeliusLicenceCategory("LAP", "Licence - Accredited Programme"),
                      subCategory = NDeliusLicenceCategory("LC54", "Aggression Replacement Training"),
                      createdDate = LocalDate.of(2024, 6, 14),
                      updatedDate = LocalDate.of(2024, 6, 14),
                    ),
                  ),
              ),
            ),
        )
    /*
    {
  "sentences": [
    {
      "eventNumber": 3,
      "licenceConditions": [
        {
          "mainCategory": {
            "code": "LAP",
            "description": "Licence - Accredited Programme"
          },
          "subCategory": {
            "code": "LC54",
            "description": "Aggression Replacement Training"
          },
          "createdDate": "2024-06-14",
          "updatedDate": "2024-06-14"
        }
      ]
    }
  ]
}

     */

      beforeEach {
        nDeliusMockServer.start()
        nDeliusMockServer.stubForGet(
          path,
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/ndelius/fixtures/LicenceConditionsResponse.json",
          ).readText(),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("nDelius", requestContext)).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        nDeliusMockServer.stop()
      }

      it("returns licence conditions for a person") {
        val response = nDeliusGateway.getLicenceConditionsForPerson(hmppsId, requestContext)
        response.data shouldBe expected
      }

      it("upstream licence conditions endpoint returns a 404") {
        nDeliusMockServer.stubForGet(
          path,
          "",
          status = HttpStatus.NOT_FOUND,
        )

        val response = nDeliusGateway.getLicenceConditionsForPerson(hmppsId, requestContext)
        response.data shouldBe null
        response.errors shouldContain UpstreamApiError(causedBy = UpstreamApi.NDELIUS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
