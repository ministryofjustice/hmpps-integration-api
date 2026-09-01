package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.RemandAndSentencingGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtCasesSummary
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

    return Response(sentencedCourtCases.data.toCourtCasesSummary())
  }
}
