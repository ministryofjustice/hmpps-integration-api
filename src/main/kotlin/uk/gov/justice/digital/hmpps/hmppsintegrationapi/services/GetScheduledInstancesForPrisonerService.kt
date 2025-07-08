package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetScheduledInstancesForPrisonerService(
  @Autowired private val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    hmppsId: String,
    startDate: String,
    endDate: String,
    slot: String?,
    filters: ConsumerFilters?,
  ): Response<List<ActivityScheduledInstanceForPrisoner>?> {
    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ActivityScheduledInstanceForPrisoner>?>(prisonId, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val response = activitiesGateway.getScheduledInstancesForPrisoner(prisonId, hmppsId, startDate, endDate, slot)
    if (response.errors.isNotEmpty()) {
      return Response(data = null, errors = response.errors)
    }

    return Response(
      data = response.data?.map { it.toActivityScheduledInstanceForPrisoner() },
      errors = response.errors,
    )
  }
}
