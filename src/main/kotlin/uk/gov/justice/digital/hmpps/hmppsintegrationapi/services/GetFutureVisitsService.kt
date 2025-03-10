package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetFutureVisitsService(
  @Autowired val prisonVisitsGateway: PrisonVisitsGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<List<Visit>?> {
    val (person, personErrors) = getPersonService.getPersonWithPrisonFilter(hmppsId, filters)
    if (personErrors.isNotEmpty()) {
      return Response(data = null, errors = personErrors)
    }

    val nomisNumber = person?.identifiers?.nomisNumber
    if (nomisNumber == null) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "No Nomis number found for $hmppsId")))
    }

    val response = prisonVisitsGateway.getFutureVisits(nomisNumber)

    return Response(data = response.data?.map { it.toVisit() }, errors = response.errors)
  }
}
