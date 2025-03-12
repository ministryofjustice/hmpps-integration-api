package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonProtectedCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetProtectedCharacteristicsService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PersonProtectedCharacteristics?> {
    val probationOffender = probationOffenderSearchGateway.getOffender(hmppsId)

    if (probationOffender.data != null) {
      val prisonOffender =
        probationOffender.data.otherIds.nomsNumber
          ?.let { prisonerOffenderSearchGateway.getPrisonOffender(it) }
      if (filters != null) {
        val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<PersonProtectedCharacteristics>(prisonOffender?.data?.prisonId, filters)
        if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
          return consumerPrisonFilterCheck
        }
      }

      val result = probationOffender.data.toPersonProtectedCharacteristics()
      if (prisonOffender?.data != null) {
        result.maritalStatus = prisonOffender.data.maritalStatus

        if (prisonOffender.data.bookingId != null) {
          result.reasonableAdjustments = nomisGateway.getReasonableAdjustments(prisonOffender.data.bookingId).data
        }
      }
      return Response(data = result, errors = probationOffender.errors)
    }
    return Response(data = null, errors = probationOffender.errors)
  }
}
