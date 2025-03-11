package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCellLocationForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<CellLocation?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = CellLocation(), errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = CellLocation(),
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonResponse = prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = nomisNumber)
    val cellLocation =
      if (prisonResponse.data?.inOutStatus == "IN") {
        CellLocation(prisonCode = prisonResponse.data.prisonId, prisonName = prisonResponse.data.prisonName, cell = prisonResponse.data.cellLocation)
      } else {
        CellLocation()
      }

    return Response(
      data = cellLocation,
      errors = prisonResponse.errors,
    )
  }
}
