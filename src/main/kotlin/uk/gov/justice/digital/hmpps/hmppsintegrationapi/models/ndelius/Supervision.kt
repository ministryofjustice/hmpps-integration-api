package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Sentence as NDeliusSentence

data class Supervision(
  val active: Boolean? = null,
  val custodial: Boolean,
  val additionalOffences: List<AdditionalOffence> = listOf(AdditionalOffence()),
  val courtAppearances: List<CourtAppearance> = listOf(CourtAppearance()),
  val mainOffence: MainOffence = MainOffence(),
  val sentence: NDeliusSentence = NDeliusSentence(),
) {
  fun toOffences(): List<Offence> {
    val courtDates = this.courtAppearances.mapNotNull { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    return listOf(this.mainOffence.toOffence(courtDates)) + this.additionalOffences.map { it.toOffence(courtDates) }
  }

  fun toSentence(): IntegrationApiSentence {
    return IntegrationApiSentence(
      dataSource = UpstreamApi.NDELIUS,
      dateOfSentencing = if (!this.sentence.date.isNullOrEmpty()) LocalDate.parse(this.sentence.date) else null,
      description = this.sentence.description,
      fineAmount = null,
      isActive = this.active,
      isCustodial = this.custodial,
      length = this.sentence.toLength(),
    )
  }
}
