package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitOrders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.visits.VisitBalances
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitOrdersForPersonService::class],
)
class GetVisitOrdersForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getVisitOrdersForPersonService: GetVisitOrdersForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val filters = ConsumerFilters(null)
    val exampleVisitBalances = VisitBalances(remainingVo = 1073741824, remainingPvo = 1073741824)
    val exampleVisitOrders = VisitOrders(remainingVisitOrders = 1073741824, remainingPrivilegeVisitOrders = 1073741824)

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(getPersonService)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(nomisGateway.getVisitBalances(nomisNumber)).thenReturn(Response(data = exampleVisitBalances))
    }

    it("returns a prisoners visit balances for a valid HMPPS ID") {
      val response = getVisitOrdersForPersonService.execute(hmppsId, filters)
      response.data shouldBe (exampleVisitOrders)
    }

    it("returns an error when getNomisNumberWithPrisonFilter returns an error") {
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )

      val response = getVisitOrdersForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }

    it("records 404 when Nomis number is null") {
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(
        Response(
          data = NomisNumber(nomisNumber = null),
        ),
      )

      val response = getVisitOrdersForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("return an error when nomisGateway.getVisitBalances returns an error") {
      whenever(nomisGateway.getVisitBalances(nomisNumber)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.NOMIS,
              ),
            ),
        ),
      )

      val response = getVisitOrdersForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
