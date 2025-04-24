package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonApi

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import java.time.LocalDate

data class PrisonApiAlert(
  val offenderNo: String? = null,
  val alertType: String? = null,
  val alertTypeDescription: String? = null,
  val alertCode: String? = null,
  val alertCodeDescription: String? = null,
  val comment: String? = null,
  val dateCreated: LocalDate? = null,
  val dateExpires: LocalDate? = null,
  val expired: Boolean? = null,
  val active: Boolean? = null,
) {
  fun toAlert(): Alert =
    Alert(
      offenderNo = this.offenderNo,
      type = this.alertType,
      typeDescription = this.alertTypeDescription,
      code = this.alertCode,
      codeDescription = this.alertCodeDescription,
      comment = this.comment,
      dateCreated = this.dateCreated,
      dateExpired = this.dateExpires,
      expired = this.expired,
      active = this.active,
    )
}
