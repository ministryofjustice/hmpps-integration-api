package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RemandAndSentencingGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtCasesSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtOutcome
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@Service
class CourtCaseService(
  val remandAndSentencingGateway: RemandAndSentencingGateway,
  val personService: GetPersonService,
) {
  fun getCourtCaseDetails(
    hmppsId: String,
    requestContext: RequestContext? = null,
  ): Response<CourtCasesSummary?> {
    val nomisNumberResponse = personService.getNomisNumber(hmppsId, requestContext?.filters)
    if (nomisNumberResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = nomisNumberResponse.errors)
    }
    val nomisNumber = nomisNumberResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.REMAND_AND_SENTENCING, UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    val sentencedCourtCases = remandAndSentencingGateway.getSentencedCourtCases(nomisNumber, requestContext)
    if (sentencedCourtCases.errors.isNotEmpty()) {
      return Response(data = null, errors = sentencedCourtCases.errors)
    }

    val dateOfFirstConviction =
      sentencedCourtCases.data.courtCases
        .flatMap { case ->
          (case.latestAppearance?.charges?.map { it } ?: emptyList()) + case.appearances.flatMap { it.charges }
        }.mapNotNull { it.sentence?.convictionDate }
        .minOrNull()

    val allCourtAppearances = sentencedCourtCases.data.courtCases.flatMap { case ->
      (case.appearances + listOf(case.latestAppearance))
    }.sortedByDescending { it?.appearanceDate }

    val courtOutcome = allCourtAppearances.firstOrNull()?.outcome?.let { CourtOutcome(it.outcomeType, it.outcomeName) }

    val courtCode = allCourtAppearances.firstOrNull { it?.outcome?.outcomeType == "SENTENCING" }?.courtCode

    return Response(CourtCasesSummary(dateOfFirstConviction = dateOfFirstConviction, courtOutcome = courtOutcome, courtCode = courtCode))
  }
}
