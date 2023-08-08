package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence

data class Supervisions (
  val supervisions: List<Supervision>,
) {
  fun toOffences(): List<Offence> {
    return this.supervisions.flatMap { it.toOffences() }
  }
}
