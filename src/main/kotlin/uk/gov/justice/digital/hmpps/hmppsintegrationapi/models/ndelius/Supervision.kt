package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence

data class Supervision(
  val mainOffence: MainOffence = MainOffence(),
  val additionalOffences: List<AdditionalOffence> = listOf(AdditionalOffence()),
  val courtAppearances: List<CourtAppearance> = listOf(CourtAppearance()),
) {
  fun toOffences(): List<Offence> {
    return listOf(
      this.mainOffence.toOffence(
        this.courtAppearances
      )
    ) + this.additionalOffences.map {
      it.toOffence(
        this.courtAppearances
      )
    }
  }
}
