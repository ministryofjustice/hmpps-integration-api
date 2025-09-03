package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonalCareNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCareNeedsForPersonService(
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
      hmppsId: String,
      filters: ConsumerFilters? = null,
  ): Response<List<PersonalCareNeed>?> {
    val personResponse = getPersonService.getPersonWithPrisonFilter(hmppsId = hmppsId, filters = filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val careNeeds = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)

    return Response(
      data = careNeeds.data?.toPersonalCareNeeds(),
      errors = careNeeds.errors,
    )
  }
}
