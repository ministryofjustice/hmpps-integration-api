package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import java.time.LocalDate

data class PrisonerAlias(
  val firstName: String,
  val lastName: String,
  val middleNames: String? = null,
  var dateOfBirth: LocalDate? = null,
) {
  fun toAlias(): Alias = Alias(
    firstName = this.firstName,
    lastName = this.lastName,
    middleName = this.middleNames,
    dateOfBirth = this.dateOfBirth,
  )
}
