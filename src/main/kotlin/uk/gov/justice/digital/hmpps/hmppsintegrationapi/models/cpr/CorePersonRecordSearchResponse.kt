package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.UserAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.LaoUserRedactable

data class CorePersonRecordSearchResponse(
  val data: List<CorePersonRecordSearchResponseGroup>,
) : LaoUserRedactable {
  override fun redact(
    userAccess: (String, List<String>) -> UserAccess?,
    deliusUsername: String,
  ): CorePersonRecordSearchResponse {
    val crns = laoCrnsForUser(userAccess, deliusUsername)
    val groups =
      data.map { group ->
        val items =
          group.results.map { item ->
            if (crns.contains(item.deliusCrn())) item.redacted() else item
          }
        CorePersonRecordSearchResponseGroup(items)
      }
    return CorePersonRecordSearchResponse(groups)
  }

  override fun crns(): List<String> = this.data.flatMap { it.results.mapNotNull { it.deliusCrn() } }
}
