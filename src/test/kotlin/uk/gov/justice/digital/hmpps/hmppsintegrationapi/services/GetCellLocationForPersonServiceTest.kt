package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

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
      val persona = personInProbationAndNomisPersona
      val hmppsId = persona.identifiers.nomisNumber!!
      val filters = RoleFilters(null)
      val prisonId = "MDI"
      val prisonName = "Moorland (HMP & YOI)"
      val cellLocation = "6-2-006"
      val prisoner =
        POSPrisoner(
          firstName = persona.firstName,
          lastName = persona.lastName,
          inOutStatus = "IN",
          prisonId = prisonId,
          prisonName = prisonName,
          cellLocation = cellLocation,
          youthOffender = false,
        )

      beforeEach {
        Mockito.reset(getPersonService)

        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(Response(data = prisoner))
      }

      it("calls getNomisNumberWithPrisonFilter") {
        getCellLocationForPersonService.execute(hmppsId, filters)
        verify(getPersonService, VerificationModeFactory.times(1)).getNomisNumberWithPrisonFilter(hmppsId, filters)
      }

      it("returns a person cell location") {
        val response = getCellLocationForPersonService.execute(hmppsId, filters)
        response.data.shouldBe(CellLocation(cell = cellLocation, prisonCode = prisonId, prisonName = prisonName))
      }

      it("returns the upstream error when an error occurs") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val response = getCellLocationForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(errors)
      }

      it("returns the upstream error when nomis id is not found") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          )
        whenever(prisonerOffenderSearchGateway.getPrisonOffender(hmppsId)).thenReturn(
          Response(
            data = null,
            errors,
          ),
        )

        val response = getCellLocationForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(errors)
      }

      it("failed to get prisoners nomis number") {
        whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber()))

        val response = getCellLocationForPersonService.execute(hmppsId, filters)
        response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
      }
    },
  )
