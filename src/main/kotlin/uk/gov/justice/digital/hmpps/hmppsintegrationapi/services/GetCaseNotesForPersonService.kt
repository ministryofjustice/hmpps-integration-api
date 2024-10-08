package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.filters.CaseNoteFilter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

@Service
class GetCaseNotesForPersonService(
  @Autowired val caseNotesGateway: CaseNotesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(filter: CaseNoteFilter): Response<List<CaseNote>> {
    val personResponse = getPersonService.execute(hmppsId = filter.hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    var caseNotes: Response<List<CaseNote>> = Response(data = emptyList())

    if (nomisNumber != null) {
      caseNotes = caseNotesGateway.getCaseNotesForPerson(id = nomisNumber, filter)
    }

    return Response(
      data = caseNotes.data,
      errors = personResponse.errors + caseNotes.errors,
    )
  }
}
