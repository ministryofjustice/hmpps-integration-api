package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

data class UpstreamApiError(val causedBy: UpstreamApi, val type: Type, val description: String? = null) {
  enum class Type {
    ENTITY_NOT_FOUND, ATTRIBUTE_NOT_FOUND
  }
}