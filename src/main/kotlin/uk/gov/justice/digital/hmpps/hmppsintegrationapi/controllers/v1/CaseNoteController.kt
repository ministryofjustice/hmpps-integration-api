package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PageCaseNote
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCaseNoteForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@RestController
@RequestMapping("/v1/caseNotes")
class CaseNoteController(
  @Autowired val getCaseNoteForPersonService: GetCaseNoteForPersonService,
  @Autowired val auditService: AuditService,
) {
  @GetMapping("{id}")
  fun getCaseNotesForPerson(
    @PathVariable id: String,
  ): Response<PageCaseNote> {
    val response = getCaseNoteForPersonService.execute(id)

    if (!response.errors.isEmpty()) {
      // do something?
    }

    return response
  }
}
