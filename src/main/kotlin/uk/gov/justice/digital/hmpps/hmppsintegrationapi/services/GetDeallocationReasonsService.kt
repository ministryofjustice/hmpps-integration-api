package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeallocationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetDeallocationReasonsService(
  @Autowired val activitiesGateway: ActivitiesGateway,
) {
  fun execute(): Response<List<DeallocationReason>?> {
    val deallocationResponse = activitiesGateway.getDeallocationReasons()
    if (deallocationResponse.errors.isNotEmpty()) {
      return Response(
        data = null,
        errors = deallocationResponse.errors,
      )
    }

    return Response(
      data = deallocationResponse.data?.map { it.toDeallocationReason() },
    )
  }
}
