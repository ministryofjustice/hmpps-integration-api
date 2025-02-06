package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.KeyDatesAndAdjustmentsDTO
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetLatestSentenceKeyDatesAndAdjustmentsForPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway,
  @Autowired val consumerPrisonAccessService: ConsumerPrisonAccessService,
) {
  fun execute(
    hmppsId: String,
    filters: ConsumerFilters?,
  ): Response<LatestSentenceKeyDatesAndAdjustments?> {
    val (person, personErrors) = probationOffenderSearchGateway.getPerson(hmppsId)

    if (personErrors.isNotEmpty()) {
      return Response(data = null, personErrors)
    }

    val nomisNumber = person?.identifiers?.nomisNumber ?: return Response(data = null)

    val (prisonerOffender, prisonOffenderErrors) = prisonerOffenderSearchGateway.getPrisonOffender(nomisNumber)
    if (prisonOffenderErrors.isNotEmpty()) {
      return Response(data = null, prisonOffenderErrors)
    }
    val prisonId = prisonerOffender?.prisonId

    val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<LatestSentenceKeyDatesAndAdjustments>(prisonId, filters)
    if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
      return consumerPrisonFilterCheck
    }

    val (latestSentenceKeyDates, latestSentenceKeyDatesErrors) =
      nomisGateway.getLatestSentenceKeyDatesForPerson(
        nomisNumber,
      )
    val (latestSentenceAdjustments, latestSentenceAdjustmentsErrors) =
      nomisGateway.getLatestSentenceAdjustmentsForPerson(
        nomisNumber,
      )

    if (latestSentenceKeyDatesErrors.isNotEmpty() && latestSentenceAdjustmentsErrors.isNotEmpty()) {
      return Response(data = null, latestSentenceKeyDatesErrors + latestSentenceAdjustmentsErrors)
    }

    val combinedKeyDatesAndAdjustments =
      KeyDatesAndAdjustmentsDTO(
        adjustments = latestSentenceAdjustments,
        keyDates = latestSentenceKeyDates,
      )

    if (checkLatestSentencesDtoIsNotPopulated(combinedKeyDatesAndAdjustments)) {
      return Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.NOMIS, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    }

    return Response(
      data =
        combinedKeyDatesAndAdjustments.toLatestSentenceKeyDatesAndAdjustments(),
    )
  }

  private fun checkLatestSentencesDtoIsNotPopulated(sentenceDetails: KeyDatesAndAdjustmentsDTO): Boolean = sentenceDetails.keyDates == null && sentenceDetails.adjustments == null
}
