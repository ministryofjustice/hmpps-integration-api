package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class GetHmppsIdService(
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<HmppsId?> {
    val identifierType = getPersonService.identifyHmppsId(hmppsId)
    if (identifierType != GetPersonService.IdentifierType.NOMS) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.BAD_REQUEST)),
      )
    }
    val personResponse = getPersonService.execute(hmppsId.uppercase())
    var hmppsIdToReturn =
      personResponse.data?.hmppsId
    return if (hmppsIdToReturn != null) {
      Response(
        data = HmppsId(hmppsIdToReturn),
        errors = personResponse.errors,
      )
    } else {
      val prisonerResponse = getPersonService.getPersonFromNomis(hmppsId.uppercase())
      hmppsIdToReturn = prisonerResponse.data?.prisonerNumber
      Response(
        data = HmppsId(hmppsIdToReturn),
        errors = prisonerResponse.errors,
      )
    }
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    val nomisResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    return Response(
      data = NomisNumber(nomisNumber = nomisResponse.data?.nomisNumber),
      errors = nomisResponse.errors,
    )
  }
}
