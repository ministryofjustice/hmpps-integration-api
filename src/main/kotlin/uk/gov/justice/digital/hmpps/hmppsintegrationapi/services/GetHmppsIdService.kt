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
    val personResponse = getPersonService.execute(hmppsId)

    val hmppsIdToReturn =
      personResponse.data?.hmppsId ?: run {
        // Attempt to look up the person in NOMIS if not found in the probation offender search
        getPersonService.getPersonFromNomis(hmppsId).data?.prisonerNumber
      }

    return Response(
      data = HmppsId(hmppsIdToReturn),
      errors = personResponse.errors,
    )
  }

  fun getNomisNumber(hmppsId: String): Response<NomisNumber?> {
    val nomisResponse = getPersonService.getNomisNumber(hmppsId = hmppsId)

    return Response(
      data = NomisNumber(nomisNumber = nomisResponse.data?.nomisNumber),
      errors = nomisResponse.errors,
    )
  }
}
