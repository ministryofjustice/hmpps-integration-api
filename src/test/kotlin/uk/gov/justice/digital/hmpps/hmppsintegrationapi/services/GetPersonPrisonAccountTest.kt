package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.NomisPersonAccount


@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPersonPrisonAccountService::class],
)
internal class GetPersonPrisonAccountTest(@MockBean val nomisGateway: NomisGateway,
                                          private val getPersonPrisonAccountService: GetPersonPrisonAccountService,
                                          @MockBean val getPersonService: GetPersonService) : DescribeSpec(
    {
      val hmppsId = "2003/13116M"

      beforeEach {
        Mockito.reset(nomisGateway)
        Mockito.reset(getPersonService)

        whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(
          Response(data = NomisNumber(nomisNumber = "123456")),
        )

      }

      it("returns a valid NomisPersonAccount when given a valid HMPPS ID") {
        val nomisNumber = "A5553AA"
        val nomisPersonAccount = NomisPersonAccount(
          cash = 100.0,
          currency = "GBP",
          damageObligations = 50.0,
          savings = 200.0,
          spends = 150.0,
          nomisNumber = nomisNumber,
          hmppsId = hmppsId
        )

        whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Response(data = NomisNumber(nomisNumber = nomisNumber)))
        whenever(nomisGateway.getOffendersAccount(nomisNumber)).thenReturn(Response(data = nomisPersonAccount))

        val response = getPersonPrisonAccountService.execute(hmppsId)

        response.data shouldBe nomisPersonAccount
      }

      it("throws EntityNotFoundException when no nomis number is found for the given HMPPS ID") {

        whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Response(data = null))

        shouldThrow<EntityNotFoundException> {
          getPersonPrisonAccountService.execute(hmppsId)
        }
      }

      it("throws EntityNotFoundException when no prisoner account is found for the given nomis number") {
        val nomisNumber = "A5553AA"

        whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Response(data = NomisNumber(nomisNumber = nomisNumber)))
        whenever(nomisGateway.getOffendersAccount(nomisNumber)).thenReturn(Response(data = NomisPersonAccount(cash = null)))

        shouldThrow<EntityNotFoundException> {
          getPersonPrisonAccountService.execute(hmppsId)
        }
      }

      it("returns errors from upstream APIs when they occur") {
        val nomisNumber = "A5553AA"
        val upstreamError = UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)

        whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(Response(data = NomisNumber(nomisNumber = nomisNumber)))
        whenever(nomisGateway.getOffendersAccount(nomisNumber)).thenReturn(Response(data = NomisPersonAccount(cash = 100.0), errors = listOf(upstreamError)))

        val response = getPersonPrisonAccountService.execute(hmppsId)

        response.errors shouldContain upstreamError
      }
    }
  )
