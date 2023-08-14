package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm

data class Term(
  val years: Int? = null,
  val months: Int? = null,
  val weeks: Int? = null,
  val days: Int? = null,
) {
  fun toTerm(): IntegrationApiTerm = IntegrationApiTerm(
    years = this.years,
    months = this.months,
    weeks = this.weeks,
    days = this.days,
  )
}
