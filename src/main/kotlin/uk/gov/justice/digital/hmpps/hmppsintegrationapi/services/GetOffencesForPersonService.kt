package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response

@Service
class GetOffencesForPersonService(
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val nDeliusGateway: NDeliusGateway,
) {
  fun execute(pncId: String): Response<List<Offence>> {
    val responseFromPrisonerOffenderSearch = prisonerOffenderSearchGateway.getPersons(pncId = pncId)
    val responseFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(pncId = pncId)

    if (responseFromPrisonerOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromPrisonerOffenderSearch.errors)
    }

    if (responseFromProbationOffenderSearch.errors.isNotEmpty()) {
      return Response(emptyList(), responseFromProbationOffenderSearch.errors)
    }

    val nomisNumber = responseFromPrisonerOffenderSearch.data.firstOrNull()?.identifiers?.nomisNumber
    val deliusCrn = responseFromProbationOffenderSearch.data?.identifiers?.deliusCrn

    var nomisOffences: Response<List<Offence>> = Response(data = emptyList())

    if(nomisNumber != null) {
      nomisOffences = nomisGateway.getOffencesForPerson(nomisNumber)

      if (nomisOffences.errors.isNotEmpty()) {
        return Response(emptyList(), nomisOffences.errors)
      }
    }

    var nDeliusOffences: Response<List<Offence>> = Response(data = emptyList())

    if(deliusCrn != null) {
      nDeliusOffences = nDeliusGateway.getOffencesForPerson(deliusCrn)

      if (nDeliusOffences.errors.isNotEmpty()) {
        return Response(emptyList(), nDeliusOffences.errors)
      }
    }

    return Response(data = nomisOffences.data + nDeliusOffences.data)
  }
}
