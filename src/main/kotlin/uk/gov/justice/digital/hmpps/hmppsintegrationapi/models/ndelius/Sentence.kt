package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Length as IntegrationApiLength

data class Sentence(
  val date: String? = null,
  val description: String? = null,
  val length: Int? = null,
  val lengthUnits: String? = null,
) {
  fun toLength(): IntegrationApiLength {
    return IntegrationApiLength(
      duration = this.length,
      units = this.lengthUnits?.lowercase(),
      terms = emptyList(),
    )
  }
}
