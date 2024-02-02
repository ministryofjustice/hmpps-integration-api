package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
) {
  fun execute(hmppsId: String): Response<Person?> {
    val personFromProbationOffenderSearch = probationOffenderSearchGateway.getPerson(id = hmppsId)

    return Response(data = personFromProbationOffenderSearch.data, errors = personFromProbationOffenderSearch.errors)
  }
}
