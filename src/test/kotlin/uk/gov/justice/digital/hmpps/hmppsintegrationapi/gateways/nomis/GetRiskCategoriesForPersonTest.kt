package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonApiGateway::class],
)
class GetRiskCategoriesForPersonTest(
    @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
    val prisonApiGateway: PrisonApiGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.PRISON_API)
      val offenderNo = "A7796DY"
      val offenderPath = "/api/offenders/$offenderNo"
      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          offenderPath,
          """
            {
            "offenderNo": "A7796DY",
            "assessments": [
                {
                "classificationCode": "C"
                }
             ]
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
        prisonApiGateway.getRiskCategoriesForPerson(offenderNo)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubForGet(offenderPath, "", HttpStatus.NOT_FOUND)

        val response = prisonApiGateway.getRiskCategoriesForPerson(offenderNo)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISON_API)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
