package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm

data class Term(
  val years: Int? = null,
  val months: Int? = null,
  val weeks: Int? = null,
  val days: Int? = null,
  val code: String? = null,
) {
  fun toTerm(): IntegrationApiTerm = IntegrationApiTerm(
    years = if (this.years != 0) this.years else null,
    months = if (this.months != 0) this.months else null,
    weeks = if (this.weeks != 0) this.weeks else null,
    days = if (this.days != 0) this.days else null,
    prisonTermCode = this.code,
  )
}
