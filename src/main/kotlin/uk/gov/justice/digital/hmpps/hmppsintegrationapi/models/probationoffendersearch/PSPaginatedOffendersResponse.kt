package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.LaoUserRedactable

data class PSPaginatedOffendersResponse(
  val content: List<Offender>,
) : LaoUserRedactable {
  override fun redact(
    userAccess: (String, List<String>) -> UserAccess?,
    deliusUsername: String,
  ): PSPaginatedOffendersResponse {
    val crns = laoCrnsForUser(userAccess, deliusUsername)
    val content =
      content.map { item ->
        if (crns.contains(item.deliusCrn())) item.redacted() else item
      }
    return PSPaginatedOffendersResponse(content)
  }

  override fun crns(): List<String> = content.mapNotNull { it.deliusCrn() }
}
