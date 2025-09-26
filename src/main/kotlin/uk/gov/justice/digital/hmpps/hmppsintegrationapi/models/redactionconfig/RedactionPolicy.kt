package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.redactionconfig

data class RedactionPolicy(
  val name: String? = null,
  val responseRedactions: List<ResponseRedaction>? = null,
)

data class ResponseRedaction(
  val type: RedactionType,
  val includes: List<String>? = emptyList(),
)

enum class RedactionType {
  REMOVE,
  MASK,
}
