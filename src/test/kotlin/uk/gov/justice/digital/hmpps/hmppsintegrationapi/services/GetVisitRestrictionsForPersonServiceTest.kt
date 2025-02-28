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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonVisitRestriction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitRestrictionsForPersonService::class],
)
class GetVisitRestrictionsForPersonServiceTest(
  @MockitoBean val nomisGateway: NomisGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getVisitRestrictionsForPersonService: GetVisitRestrictionsForPersonService,
) : DescribeSpec({
    val hmppsId = "1234/56789B"
    val nomisNumber = "Z99999ZZ"
    val filters = ConsumerFilters(null)
    val examplePersonVisitRestrictions =
      listOf(
        PersonVisitRestriction(restrictionId = 1, comment = "Restriction 1", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
        PersonVisitRestriction(restrictionId = 2, comment = "Restriction 2", restrictionType = "TYPE", restrictionTypeDescription = "Type description", startDate = "2025-01-01", expiryDate = "2025-12-31", active = true),
      )

    beforeEach {
      Mockito.reset(nomisGateway)
      Mockito.reset(getPersonService)

      require(hmppsId.matches(Regex("^[0-9]+/[0-9A-Za-z]+$"))) {
        "Invalid Hmpps Id format: $hmppsId"
      }

      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId = hmppsId, filters)).thenReturn(
        Response(data = NomisNumber(nomisNumber = nomisNumber)),
      )

      whenever(nomisGateway.getOffenderVisitRestrictions(nomisNumber)).thenReturn(Response(data = examplePersonVisitRestrictions))
    }

    it("returns visitor restrictions for a valid HMPPS ID") {
      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response.data shouldBe (examplePersonVisitRestrictions)
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

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
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

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("return an error when nomisGateway.getOffenderVisitRestrictions returns an error") {
      whenever(nomisGateway.getOffenderVisitRestrictions(nomisNumber)).thenReturn(
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

      val response = getVisitRestrictionsForPersonService.execute(hmppsId, filters)
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.NOMIS,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
