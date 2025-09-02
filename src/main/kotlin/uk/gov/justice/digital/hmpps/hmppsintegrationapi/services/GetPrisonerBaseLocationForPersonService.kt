package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerBaseLocationGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonerBaseLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetPrisonerBaseLocationForPersonService(
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired private val getPersonService: GetPersonService,
  @Autowired private val prisonerBaseLocationGateway: PrisonerBaseLocationGateway,
) {
  fun execute(
    hmppsId: String,
    filters: RoleFilters?,
  ): Response<PrisonerBaseLocation?> {
    val personResponse = getPersonService.getNomisNumber(hmppsId)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = personResponse.errors)
    }

    val nomisNumber =
      personResponse.data?.nomisNumber ?: return Response(
        data = null,
        errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)),
      )

    val prisonerBaseLocationResponse = prisonerBaseLocationGateway.getPrisonerBaseLocation(nomisNumber)
    if (prisonerBaseLocationResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = prisonerBaseLocationResponse.errors)
    }

    val prisonId = prisonerBaseLocationResponse.data?.run { if (inPrison == true) prisonId else lastPrisonId }
    if (prisonId == null) {
      return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    }

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonerBaseLocation>(prisonId, filters)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    return prisonerBaseLocationResponse
  }
}
