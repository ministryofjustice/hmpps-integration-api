package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetLocationByKeyService(
  @Autowired val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    prisonId: String,
    key: String,
    filters: ConsumerFilters?,
  ): Response<Location?> {
    val checkAccess = consumerPrisonAccessService.checkConsumerHasPrisonAccess<Location?>(prisonId, filters)

    if (checkAccess.errors.isNotEmpty()) {
      return Response(data = checkAccess.data, errors = checkAccess.errors)
    }

    val prisonCombinedWithLocationKey = "$prisonId-$key"
    val result = locationsInsidePrisonGateway.getLocationByKey(prisonCombinedWithLocationKey)

    if (result.errors.isNotEmpty()) {
      return Response(data = null, errors = result.errors)
    }

    return Response(data = result.data?.toLocation())
  }
}
