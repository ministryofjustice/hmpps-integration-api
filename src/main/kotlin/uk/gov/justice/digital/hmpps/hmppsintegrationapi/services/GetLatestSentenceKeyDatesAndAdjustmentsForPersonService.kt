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

    if (latestSentenceAdjustments) {
    }

    return Response(
      data =
        KeyDatesAndAdjustmentsDTO(
          adjustments = latestSentenceAdjustments,
          keyDates = latestSentenceKeyDates,
        ).toLatestSentenceKeyDatesAndAdjustments(),
    )
  }

  private fun checkisNotPopulated()
}

/**
 * Cases:
 *
 * Nomis number, POS success, Nomis success →  responses
 *
 * Nomis number, POS success, Nomis 404 → Ideally return just POS response
 *
 * Nomis number, POS success, nomis non-404 error → Return NOMIS error
 *
 * Nomis number, POS 404, nomis success → Return just NOMIS
 *
 * Nomis number, POS 404, nomis any error (incl. 404) → Return just NOMIS
 *
 * Nomis number, POS non-404 error → Return NOMIS response
 *
 * Person service error → Return person service error
 *
 * No nomis number → return error

 */
