package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCellLocationForPersonService::class],
)
internal class GetCellLocationForPersonServiceTest(
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  private val getCellLocationForPersonService: GetCellLocationForPersonService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = ConsumerFilters(null)

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))

        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(data = POSPrisoner(firstName = "Qui-gon", lastName = "Jin", inOutStatus = "IN", prisonId = "MDI", prisonName = "Moorland (HMP & YOI)", cellLocation = "6-2-006")),
        )
      }

      it("calls getNomisNumberWithPrisonFilter") {
        getCellLocationForPersonService.execute(hmppsId, filters)

        verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
      }

      it("returns a person cell location") {
        val response = getCellLocationForPersonService.execute(hmppsId, filters)

        response.data.shouldBe(CellLocation(cell = "6-2-006", prisonCode = "MDI", prisonName = "Moorland (HMP & YOI)"))
      }

      it("returns the upstream error when an error occurs") {
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.NOMIS,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getCellLocationForPersonService.execute(hmppsId, filters)

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

      it("returns the upstream error when nomis id is not found") {
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                ),
              ),
          ),
        )

        val response = getCellLocationForPersonService.execute(hmppsId, filters)

        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.PRISONER_OFFENDER_SEARCH)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }
    },
  )
