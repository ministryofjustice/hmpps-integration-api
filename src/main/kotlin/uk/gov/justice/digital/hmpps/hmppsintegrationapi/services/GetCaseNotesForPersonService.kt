package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@Service
class GetCaseNotesForPersonService(
  @Autowired val caseNotesGateway: CaseNotesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(
    filter: CaseNoteFilter,
    filters: ConsumerFilters?,
  ): Response<List<CaseNote>?> {
    val personResponse = getPersonService.getNomisNumberWithPrisonFilter(filter.hmppsId, filters)
    if (personResponse.errors.isNotEmpty()) {
      return Response(data = emptyList(), errors = personResponse.errors)
    }

    val nomisNumber = personResponse.data?.nomisNumber ?: return Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND)))

    val caseNotes = caseNotesGateway.getCaseNotesForPerson(id = nomisNumber, filter)

    return Response(
      data = caseNotes.data,
      errors = caseNotes.errors,
    )
  }
}
