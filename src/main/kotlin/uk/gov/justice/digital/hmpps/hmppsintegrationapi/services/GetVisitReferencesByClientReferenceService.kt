package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitReference
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetVisitReferencesByClientReferenceService(
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired private val prisonVisitsGateway: PrisonVisitsGateway,
) {
  fun execute(
    clientReference: String,
    filters: ConsumerFilters? = null,
  ): Response<List<VisitReference>?> {
    val visitReferencesResponse = prisonVisitsGateway.getVisitReferencesByClientReference(clientReference)

    if (!visitReferencesResponse.errors.isNullOrEmpty()) {
      return Response(data = null, errors = visitReferencesResponse.errors)
    }

    val visitRef =
      visitReferencesResponse.data
        ?.firstOrNull()
        ?.visitReference
        .orEmpty()

    val getVisitResponse = prisonVisitsGateway.getVisitByReference(visitRef)

    val prisonId = getVisitResponse?.data?.prisonId

    if (prisonId.isNullOrBlank()) {
      return Response(
        data = null,
        errors =
          listOf(
            UpstreamApiError(
              UpstreamApi.MANAGE_PRISON_VISITS,
              UpstreamApiError.Type.ENTITY_NOT_FOUND,
              "PrisonId not found",
            ),
          ),
      )
    }

    val consumerPrisonFilterCheck =
      consumerPrisonAccessService.checkConsumerHasPrisonAccess<Visit>(
        prisonId,
        filters,
        UpstreamApi.MANAGE_PRISON_VISITS,
      )

    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return Response(data = null, errors = consumerPrisonFilterCheck.errors)
    }

    return Response(data = visitReferencesResponse.data)
  }
}
