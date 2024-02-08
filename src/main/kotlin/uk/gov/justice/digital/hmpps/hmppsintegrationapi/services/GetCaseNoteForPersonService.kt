package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.CaseNotesGateway

class GetCaseNoteForPersonService(
  @Autowired val caseNotesGateway: CaseNotesGateway,
  @Autowired val getPersonService: GetPersonService,
) {
  fun doSomething() {
  }
}
