package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialHierarchyItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters

@Component
@Service
class GetResidentialHierarchyService(
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
) {
  fun execute(
    prisonId: String,
    includeInactive: Boolean = false,
    filters: RoleFilters?,
  ): Response<List<ResidentialHierarchyItem>?> {
    val checkAccess = consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ResidentialHierarchyItem>>(prisonId, filters)
    if (checkAccess.errors.isNotEmpty()) {
      return Response(data = checkAccess.data, errors = checkAccess.errors)
    }

    val result = locationsInsidePrisonGateway.getResidentialHierarchy(prisonId, includeInactive)
    if (result.errors.isNotEmpty()) {
      return Response(data = null, errors = result.errors)
    }

    return Response(data = result.data?.map { it.toResidentialHierarchyItem() })
  }
}
