package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetOffencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<Offence>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val deliusCrn = personResponse.data["probationOffenderSearch"]?.identifiers?.deliusCrn
    val nomisNumber = personResponse.data["probationOffenderSearch"]?.identifiers?.nomisNumber

    var nomisOffences: Response<List<Offence>> = Response(data = emptyList())
    var nDeliusOffences: Response<List<Offence>> = Response(data = emptyList())

    if (nomisNumber != null) {
      nomisOffences = nomisGateway.getOffencesForPerson(nomisNumber)
    }

    if (deliusCrn != null) {
      nDeliusOffences = nDeliusGateway.getOffencesForPerson(deliusCrn)
    }

    return Response(
      data = nomisOffences.data + nDeliusOffences.data,
      errors = personResponse.errors + nomisOffences.errors + nDeliusOffences.errors,
    )
  }
}
