package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Component
@Service
class GetCapacityForPrisonService(
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
) {
  fun execute(
    prisonId: String,
    filters: RoleFilters?,
  ): Response<PrisonCapacity?> {
    val checkAccess = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonCapacity>(prisonId, filters)

    if (checkAccess.errors.isNotEmpty()) {
      return Response(data = checkAccess.data, errors = checkAccess.errors)
    }

    val result = locationsInsidePrisonGateway.getResidentialSummary(prisonId)

    if (result.errors.isNotEmpty()) {
      return Response(data = null, errors = result.errors)
    }

    return Response(data = result.data?.prisonSummary?.toPrisonCapacity())
  }
}
