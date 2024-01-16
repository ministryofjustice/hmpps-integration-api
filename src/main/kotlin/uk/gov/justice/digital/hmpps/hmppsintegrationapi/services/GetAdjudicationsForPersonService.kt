package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetAdjudicationsForPersonService(
  @Autowired val adjudicationsGateway: AdjudicationsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<List<Adjudication>> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    var adjudications: Response<List<Adjudication>> = Response(data = emptyList())

    if (nomisNumber == null) {
      return Response(
        data = emptyList(),
        errors = personResponse.errors,
      )
    } else {
      adjudications = adjudicationsGateway.getReportedAdjudicationsForPerson(id = nomisNumber)
    }

    return Response(
      data = adjudications.data,
      errors = adjudications.errors,
    )
  }
}
