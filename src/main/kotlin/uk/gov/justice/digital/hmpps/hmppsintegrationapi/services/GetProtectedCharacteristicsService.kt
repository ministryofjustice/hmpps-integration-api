package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NDeliusGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonApiGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonProtectedCharacteristics
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService.IdentifierType

@Service
class GetProtectedCharacteristicsService(
  @Autowired val nDeliusGateway: NDeliusGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val prisonApiGateway: PrisonApiGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<PersonProtectedCharacteristics?> {
    val hmppsIdType = getPersonService.identifyHmppsId(hmppsId)
    if (hmppsIdType == IdentifierType.UNKNOWN) {
      return Response(
        data = null,
        errors = listOf(UpstreamApiError(causedBy = UpstreamApi.PRISON_API, type = UpstreamApiError.Type.BAD_REQUEST)),
      )
    }

    val probationOffender = nDeliusGateway.getOffender(hmppsId)

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
          result.reasonableAdjustments = prisonApiGateway.getReasonableAdjustments(prisonOffender.data.bookingId).data
        }
      }
      return Response(data = result, errors = probationOffender.errors)
    }
    return Response(data = null, errors = probationOffender.errors)
  }
}
