package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetCellLocationForPersonService(
  @Autowired val getPersonService: GetPersonService,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<CellLocation?> {

    val prisonResponse = when {
      isNomsNumber(hmppsId) -> prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = hmppsId)
      else -> {
        val personResponse = getPersonService.execute(hmppsId = hmppsId)

        if (personResponse.data == null) {
          return Response(
            data = CellLocation(),
            errors = personResponse.errors
          )
        }

        personResponse.data.identifiers.nomisNumber?.let {
          prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = it)
        }
      }
    }

    val cellLocation =
      if (prisonResponse?.data?.inOutStatus == "IN") {
        CellLocation(prisonCode = prisonResponse.data.prisonId, prisonName = prisonResponse.data.prisonName, cell = prisonResponse.data.cellLocation)
      } else {
        CellLocation()
      }

    return Response(
      data = cellLocation,
      errors = prisonResponse?.errors ?: emptyList()
    )

  }
}
