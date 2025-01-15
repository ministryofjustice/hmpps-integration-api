package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Sentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.SystemSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class NDeliusSupervision(
  val active: Boolean? = null,
  val custodial: Boolean,
  val additionalOffences: List<NDeliusAdditionalOffence> = listOf(NDeliusAdditionalOffence()),
  val courtAppearances: List<NDeliusCourtAppearance> = listOf(NDeliusCourtAppearance()),
  val mainOffence: NDeliusMainOffence = NDeliusMainOffence(),
  val sentence: NDeliusSentence = NDeliusSentence(),
) {
  fun toOffences(): List<Offence> {
    val courtDates = this.courtAppearances.mapNotNull { LocalDate.parse(it.date, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }
    val courtName = this.courtAppearances.firstOrNull()?.court
    val mainOffence = this.mainOffence.toOffence(courtDates, courtName)
    val additionalOffences = this.additionalOffences.map { it.toOffence(courtDates, courtName) }
    return listOf(mainOffence) + additionalOffences
  }

  fun toSentence(): Sentence =
    Sentence(
      serviceSource = UpstreamApi.NDELIUS,
      systemSource = SystemSource.PROBATION_SYSTEMS,
      dateOfSentencing = if (!this.sentence.date.isNullOrEmpty()) LocalDate.parse(this.sentence.date) else null,
      description = this.sentence.description,
      fineAmount = null,
      isActive = this.active,
      isCustodial = this.custodial,
      length = this.sentence.toLength(),
    )
}
