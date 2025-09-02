package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SuitabilityCriteria
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Service
class GetActivitiesSuitabilityCriteriaService(
  @Autowired val activitiesGateway: ActivitiesGateway,
  @Autowired val getScheduleDetailsService: GetScheduleDetailsService,
) {
  fun execute(
    scheduleId: Long,
    filters: RoleFilters? = null,
  ): Response<SuitabilityCriteria?> {
    val checkPrisonCode = getScheduleDetailsService.execute(scheduleId, filters)
    if (checkPrisonCode.errors.isNotEmpty()) {
      return Response(data = null, errors = checkPrisonCode.errors)
    }

    val suitabilityCriteriaResponse = activitiesGateway.getActivitySuitabilityCriteria(scheduleId)
    return Response(
      data = suitabilityCriteriaResponse.data?.toSuitabilityCriteria(),
      errors = suitabilityCriteriaResponse.errors,
    )
  }
}
