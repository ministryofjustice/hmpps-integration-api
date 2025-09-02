package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetOffencesForPersonService(
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: RoleFilters?,
  ): Response<List<Offence>> {
    val personResponse: Response<Person?>
    val nomisNumber: String?
    var nomisOffences: Response<List<Offence>> = Response(data = emptyList())
    var nDeliusOffences: Response<List<Offence>> = Response(data = emptyList())

    if (filters?.hasPrisonFilter() == true) {
      personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId, filters)
      nomisNumber = personResponse.data?.identifiers?.nomisNumber ?: return Response(data = emptyList(), errors = personResponse.errors)
      nomisOffences = prisonApiGateway.getOffencesForPerson(nomisNumber)

      return Response(
        data = nomisOffences.data,
        errors = nomisOffences.errors,
      )
    } else {
      personResponse = getPersonService.execute(hmppsId = hmppsId)
      nomisNumber = personResponse.data?.identifiers?.nomisNumber
      val deliusCrn = personResponse.data?.identifiers?.deliusCrn

      if (nomisNumber != null) {
        nomisOffences = prisonApiGateway.getOffencesForPerson(nomisNumber)
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
}
