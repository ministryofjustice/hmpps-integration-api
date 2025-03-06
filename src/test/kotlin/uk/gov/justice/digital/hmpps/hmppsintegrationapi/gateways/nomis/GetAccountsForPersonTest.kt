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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [NomisGateway::class],
)
class GetAccountsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val nomisGateway: NomisGateway,
) : DescribeSpec(
    {
      val nomisApiMockServer = ApiMockServer.create(UpstreamApi.NOMIS)
      val nomisNumber = "AA1234Z"
      val prisonId = "XYZ"
      val accountsPath = "/api/v1/prison/$prisonId/offenders/$nomisNumber/accounts"

      beforeEach {
        nomisApiMockServer.start()
        nomisApiMockServer.stubForGet(
          accountsPath,
          """
          {
              "spends": 114217,
              "savings": 2234,
              "cash": 1000
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
        nomisGateway.getAccountsForPerson(prisonId, nomisNumber)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("NOMIS")
      }

      it("returns account balances for the matching person ID") {
        val response = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)

        response.errors.shouldBeEmpty()
        response.data?.spends.shouldBe(114217)
        response.data?.savings.shouldBe(2234)
        response.data?.cash.shouldBe(1000)
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        nomisApiMockServer.stubForGet(accountsPath, "", HttpStatus.NOT_FOUND)

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

      it("returns an error when 400 Bad Request is returned because of invalid ID") {
        nomisApiMockServer.stubForGet(accountsPath, "", HttpStatus.BAD_REQUEST)

        val response = nomisGateway.getAccountsForPerson(prisonId, nomisNumber)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.NOMIS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
      }
    },
  )
