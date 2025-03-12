package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetAdjudicationsForPersonService(
  @Autowired val adjudicationsGateway: AdjudicationsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters? = null,
  ): Response<List<Adjudication>> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    var adjudications: Response<List<Adjudication>> = Response(data = emptyList())

    if (nomisNumber != null) {
      adjudications = adjudicationsGateway.getReportedAdjudicationsForPerson(id = nomisNumber)
    }

    return Response(
      data = adjudications.data,
      errors = personResponse.errors + adjudications.errors,
    )
  }
}
