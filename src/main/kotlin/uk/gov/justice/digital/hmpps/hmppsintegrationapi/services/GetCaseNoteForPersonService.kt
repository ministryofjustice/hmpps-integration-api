package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response

class GetCaseNoteForPersonService(
  @Autowired val caseNotesGateway: CaseNotesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun execute(hmppsId: String): Response<PageCaseNote> {
    val personResponse = getPersonService.execute(hmppsId = hmppsId)
    val nomisNumber = personResponse.data?.identifiers?.nomisNumber

    var caseNotes: Response<PageCaseNote> = Response(data = PageCaseNote(null))

    if (nomisNumber != null) {
      caseNotes = caseNotesGateway.getCaseNotesForPerson(id = nomisNumber)
    }

    return Response(
      data = caseNotes.data,
      errors = personResponse.errors + caseNotes.errors,
    )
  }
}
