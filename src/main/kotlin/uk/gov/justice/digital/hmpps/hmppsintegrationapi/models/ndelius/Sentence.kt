package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm

data class Sentence(
  val date: String? = null,
  val length: Int? = null,
  val lengthUnits: String? = null,
) {
  fun toTerm(): List<IntegrationApiTerm> {
    return when (this.lengthUnits?.lowercase()) {
      "years" -> listOf(IntegrationApiTerm(years = this.length))
      "months" -> listOf(IntegrationApiTerm(months = this.length))
      "weeks" -> listOf(IntegrationApiTerm(weeks = this.length))
      "days" -> listOf(IntegrationApiTerm(days = this.length))
      else -> listOf(IntegrationApiTerm())
    }
  }
}
