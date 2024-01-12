package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SentenceTerm

data class NomisTerm(
  val years: Int? = null,
  val months: Int? = null,
  val weeks: Int? = null,
  val days: Int? = null,
  val code: String? = null,
) {
  fun toTerm(): SentenceTerm = SentenceTerm(
    years = if (this.years != 0) this.years else null,
    months = if (this.months != 0) this.months else null,
    weeks = if (this.weeks != 0) this.weeks else null,
    days = if (this.days != 0) this.days else null,
    prisonTermCode = this.code,
  )
}
