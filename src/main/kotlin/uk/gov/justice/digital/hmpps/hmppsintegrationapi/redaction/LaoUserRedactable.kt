package uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess

interface LaoUserRedactable {
  fun redact(
    userAccess: (String, List<String>) -> UserAccess?,
    deliusUsername: String,
  ): Any

  fun crns(): List<String>

  fun laoCrnsForUser(
    check: (String, List<String>) -> UserAccess?,
    deliusUsername: String,
  ): List<String> = check(deliusUsername, crns())?.laoCrns() ?: emptyList()
}
