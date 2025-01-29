package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.managePOMcase

import io.kotest.core.spec.style.DescribeSpec
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManagePOMCaseGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ManagePOMCaseApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ManagePOMCaseGateway::class],
)
class ManagePOMCaseGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val managePOMCaseGateway: ManagePOMCaseGateway,
) : DescribeSpec(
    {
      val managePOMCaseApiMockServer = ManagePOMCaseApiMockServer()
      beforeEach {
        managePOMCaseApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ManagePOMCase")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }
      afterTest {
        managePOMCaseApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        managePOMCaseGateway.getPrimaryPOMForNomisNumber(id = "X1234YZ")

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ManagePOMCase")
      }

      it("returns an error when 400 Bad Request is returned because of invalid ID") {
        managePOMCaseApiMockServer.stubGetPrimaryPOMForNomisNumber("X1234YZ", "", HttpStatus.BAD_REQUEST)
        val response = managePOMCaseGateway.getPrimaryPOMForNomisNumber(id = "X1234YZ")

        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.MANAGE_POM_CASE)
      }

      it("returns primary offender officer") {
        managePOMCaseApiMockServer.stubGetPrimaryPOMForNomisNumber(
          "X1234YZ",
          """
          {
            "manager": {
              "code": 0,
              "forename": "string",
              "surname": "string"
            },
            "prison": {
              "code": "string"
            }
          }
        """,
          HttpStatus.OK,
        )

        val response = managePOMCaseGateway.getPrimaryPOMForNomisNumber(id = "X1234YZ")
        response.data.forename.shouldBe("string")
        response.data.surname.shouldBe("string")
      }
    },
  )
