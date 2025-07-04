package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerBaseLocationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetPrisonerBaseLocationForPersonService(
  @Autowired private val getPersonService: GetPersonService,
  @Autowired private val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PrisonerBaseLocation?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    return prisonerBaseLocationGateway.getPrisonerBaseLocation(nomisNumber)
  }
}
