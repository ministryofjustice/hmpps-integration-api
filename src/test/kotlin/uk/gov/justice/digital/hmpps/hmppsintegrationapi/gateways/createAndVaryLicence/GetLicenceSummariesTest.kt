package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.createAndVaryLicence

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
class GetLicenceSummariesTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val createAndVaryLicenceGateway: CreateAndVaryLicenceGateway,
) : DescribeSpec(
    {
      val createAndVaryLicenceApiMockServer = CreateAndVaryLicenceApiMockServer()
      val deliusCrn = "X777776"

      beforeEach {
        createAndVaryLicenceApiMockServer.start()
        createAndVaryLicenceApiMockServer.stubGetLicenceSummaries(
          deliusCrn,
          """
          [{
          "id":"A1234AA",
          "prisonNumber": "1140484"
          }]
        """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("CVL")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        createAndVaryLicenceApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        createAndVaryLicenceGateway.getLicenceSummaries(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("CVL")
      }

      it("returns licences for a person with the matching ID") {
        val response = createAndVaryLicenceGateway.getLicenceSummaries(deliusCrn)

        response.data
          .first()
          .offenderNumber
          .shouldBe("1140484")
      }

      it("returns an error when 404 NOT FOUND is returned") {
        createAndVaryLicenceApiMockServer.stubGetLicenceSummaries(
          deliusCrn,
          """
        [{
          "developerMessage": "cannot find person"
        }]
        """,
          HttpStatus.NOT_FOUND,
        )

        val response = createAndVaryLicenceGateway.getLicenceSummaries(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
