package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<Person?> {
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(id = hmppsId)

    return Response(data = personFromProbationOffenderSearch.data, errors = personFromProbationOffenderSearch.errors)
  }

  fun getCombinedDataForPerson(hmppsId: String): Response<Map<String, Person?>> {
    val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)

    val prisonResponse =
      probationResponse.data?.identifiers?.nomisNumber?.let {
        prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = it)
      }

    val data =
      mapOf(
        "prisonerOffenderSearch" to prisonResponse?.data?.toPerson(),
        "probationOffenderSearch" to probationResponse.data,
      )

    val errors = (prisonResponse?.errors ?: emptyList()) + probationResponse.errors

    return Response(
      data = data,
      errors = errors,
    )
  }
}
