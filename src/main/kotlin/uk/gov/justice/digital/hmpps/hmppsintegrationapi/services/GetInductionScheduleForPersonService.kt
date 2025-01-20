package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedules
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetInductionScheduleForPersonService(
  @Autowired val plpGateway: PLPGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<InductionSchedule> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)

    // not a valid person
    if (response.errors.isNotEmpty()) {
      return Response(InductionSchedule(), response.errors)
    }

    response.data?.nomisNumber?.let {
      return plpGateway.getInductionSchedule(it)
    }
    return Response(InductionSchedule(), response.errors)
  }

  fun getHistory(hmppsId: String): Response<InductionSchedules> {
    val response = getPersonService.getNomisNumber(hmppsId = hmppsId)
    // not a valid person
    if (response.errors.isNotEmpty()) {
      return Response(InductionSchedules(listOf()), response.errors)
    }
    // found data will return it
    response.data?.nomisNumber?.let {
      return plpGateway.getInductionScheduleHistory(it)
    }
    // no data found return an empty list
    return Response(InductionSchedules(listOf()), response.errors)
  }
}
