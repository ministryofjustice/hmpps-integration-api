package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.SentenceLength
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence

data class Sentence(
  val startDate: LocalDate?,
  val days: Int?,
  val weeks: Int?,
  val months: Int?,
  val years: Int?,
  val fineAmount: Double?,
  val lifeSentence: Boolean?,
) {
  fun toSentence() = IntegrationApiSentence(
    startDate = this.startDate,
    length = SentenceLength(
      days = this.days,
      weeks = this.weeks,
      months = this.months,
      years = this.years,
    ),
    fineAmount = this.fineAmount,
    isLifeSentence = this.lifeSentence,
  )
}
