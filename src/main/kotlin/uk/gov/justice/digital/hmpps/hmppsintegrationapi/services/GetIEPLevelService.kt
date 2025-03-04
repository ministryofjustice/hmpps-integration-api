package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.IncentivesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IEPLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetIEPLevelService(
  @Autowired val incentivesGateway: IncentivesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    prisonerId: String,
    filter: ConsumerFilters?,
  ): Response<IEPLevel?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(prisonerId, filter)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val response = incentivesGateway.getIEPReviewHistory(nomisNumber)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }

    return Response(data = response.data?.toIEPLevel())
  }
}
