package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.cpr

import com.fasterxml.jackson.annotation.JsonInclude
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.ProbationOffender
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.REDACTION_MASKING_TEXT

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CorePersonRecordSearchResponseItem(
  val name: CPRName? = null,
  val aliases: List<CPRAlias> = emptyList(),
  val addresses: List<CPRAddress> = emptyList(),
  val identifiers: CPRIdentifier? = null,
  val sourceSystem: String? = null,
  val status: String? = null,
  val linkedRecords: List<CorePersonLinkedRecord> = emptyList(),
) : ProbationOffender {
  override fun deliusCrn(): String? {
    if (sourceSystem == "DELIUS") {
      return identifiers?.crn
    } else {
      return null
    }
  }

  override fun redacted(): CorePersonRecordSearchResponseItem =
    CorePersonRecordSearchResponseItem(
      CPRName(
        firstName = REDACTION_MASKING_TEXT,
        lastName = REDACTION_MASKING_TEXT,
      ),
      identifiers = this.identifiers,
      sourceSystem = this.sourceSystem,
      status = this.status,
    )
}
