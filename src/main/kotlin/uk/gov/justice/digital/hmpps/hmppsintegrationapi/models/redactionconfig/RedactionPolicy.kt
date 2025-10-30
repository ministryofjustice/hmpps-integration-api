package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.redaction.RedactionContext

interface ResponseRedaction {
  fun apply(
    redactionContext: RedactionContext,
    responseBody: Any,
  ): Any
}

data class RedactionPolicy(
  val name: String? = null,
  val responseRedactions: List<ResponseRedaction>? = null,
)

const val REDACTION_MASKING_TEXT = "**REDACTED**"

enum class RedactionType {
  REMOVE,
  MASK,
}
