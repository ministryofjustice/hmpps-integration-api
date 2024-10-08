package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OffenderSearchResponse
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

  fun getCombinedDataForPerson(hmppsId: String): Response<OffenderSearchResponse> {
    val probationResponse = probationOffenderSearchGateway.getPerson(id = hmppsId)

    val prisonResponse =
      probationResponse.data?.identifiers?.nomisNumber?.let {
        prisonerOffenderSearchGateway.getPrisonOffender(nomsNumber = it)
      }

    val data =
      OffenderSearchResponse(
        prisonerOffenderSearch = prisonResponse?.data?.toPerson(),
        probationOffenderSearch = probationResponse.data,
      )

    return Response(
      data = data,
      errors = (prisonResponse?.errors ?: emptyList()) + probationResponse.errors,
    )
  }
}
