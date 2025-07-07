package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetActivitiesSuitabilityCriteriaService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getActivitiesScheduleService: GetActivitiesScheduleService,
) {
  fun execute(
    activityId: Long,
    filters: ConsumerFilters? = null,
  ) {
  }
}
