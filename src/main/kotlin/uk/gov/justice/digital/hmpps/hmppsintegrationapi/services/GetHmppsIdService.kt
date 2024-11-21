package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsId
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetHmppsIdService(
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<HmppsId?> {
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
