package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.NomisApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetTransactionForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = NomisApiMockServer()
      val nomisNumber = "AA1234Z"
      val prisonId = "XYZ"
      val clientUniqueRef = "client_unique_ref"
      val transactionPath = "/api/v1/prison/$prisonId/offenders/$nomisNumber/transactions/$clientUniqueRef"

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubNomisApiResponse(
          transactionPath,
          """
        {
          "id": "204564839-3",
          "type": {
            "code": "spends",
            "desc": "Spends account code"
          },
          "description": "Transfer In Regular from caseload PVR",
          "amount": 12345,
          "date": "2016-10-21"
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
        nomisGateway.getTransactionForPerson(
          prisonId,
          nomisNumber,
          clientUniqueRef,
        )

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns a transaction for the matching person ID") {
        val response =
          nomisGateway.getTransactionForPerson(
            prisonId,
            nomisNumber,
            clientUniqueRef,
          )

        response.errors.shouldBeEmpty()
        response.data
          ?.id
          .shouldBe("204564839-3")
        response.data
          ?.type
          ?.code
          .shouldBe("spends")
        response.data
          ?.type
          ?.desc
          .shouldBe("Spends account code")
        response.data
          ?.description
          .shouldBe("Transfer In Regular from caseload PVR")
        response.data
          ?.amount
          .shouldBe(12345)
        response.data
          ?.date
          .shouldBe("2016-10-21")
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubNomisApiResponse(transactionPath, "", HttpStatus.NOT_FOUND)

        val response = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NOMIS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
