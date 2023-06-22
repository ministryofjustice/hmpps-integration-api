package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Alias
import java.time.LocalDate

data class OffenderAlias(
  val firstName: String,
  val surname: String,
  val middleNames: List<String> = listOf(),
  var dateOfBirth: LocalDate? = null,
  val gender: String? = null,
) {
  fun toAlias(): Alias = Alias(
    firstName = this.firstName,
    lastName = this.surname,
    middleName = this.middleNames.joinToString(" "),
    dateOfBirth = this.dateOfBirth,
    gender = this.gender,
  )
}
