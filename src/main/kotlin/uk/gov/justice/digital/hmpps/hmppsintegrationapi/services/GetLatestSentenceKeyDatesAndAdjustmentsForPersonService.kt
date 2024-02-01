package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.NomisGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ProbationOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.KeyDatesAndAdjustmentsDTO
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LatestSentenceKeyDatesAndAdjustments
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetLatestSentenceKeyDatesAndAdjustmentsForPersonService(
  @Autowired val probationOffenderSearchGateway: ProbationOffenderSearchGateway,
  @Autowired val nomisGateway: NomisGateway,
) {
  fun execute(hmppsId: String): Response<LatestSentenceKeyDatesAndAdjustments?> {
    val (person, personErrors) = probationOffenderSearchGateway.getPerson(hmppsId)

    if (personErrors.isNotEmpty()) {
      return Response(data = null, personErrors)
    }

    val nomisNumber = person?.identifiers?.nomisNumber ?: return Response(data = null)

    val (latestSentenceKeyDates, latestSentenceKeyDatesErrors) = nomisGateway.getLatestSentenceKeyDatesForPerson(
      nomisNumber,
    )
    val (latestSentenceAdjustments, latestSentenceAdjustmentsErrors) = nomisGateway.getLatestSentenceAdjustmentsForPerson(
      nomisNumber,
    )

    if (latestSentenceKeyDatesErrors.isNotEmpty() && latestSentenceAdjustmentsErrors.isNotEmpty()) {
      return Response(data = null, latestSentenceKeyDatesErrors + latestSentenceAdjustmentsErrors)
    }

    return Response(
      data = KeyDatesAndAdjustmentsDTO(
        adjustments = latestSentenceAdjustments,
        keyDates = latestSentenceKeyDates,
      ).toLatestSentenceKeyDatesAndAdjustments(),
    )
  }
}
