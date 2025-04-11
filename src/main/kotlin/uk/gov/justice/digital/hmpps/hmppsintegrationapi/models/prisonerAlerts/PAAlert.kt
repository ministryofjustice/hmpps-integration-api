package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import java.time.LocalDate
import java.time.LocalDateTime

data class PAAlert(
  val alertUuid: String,
  val prisonNumber: String,
  val alertCode: PAAlertCode,
  val description: String? = null,
  val authorisedBy: String? = null,
  val activeFrom: LocalDate,
  val activeTo: LocalDate? = null,
  val isActive: Boolean,
  val createdAt: LocalDateTime,
  val createdBy: String,
  val createdByDisplayName: String,
  val lastModifiedAt: LocalDateTime? = null,
  val lastModifiedBy: String? = null,
  val lastModifiedByDisplayName: String? = null,
  val activeToLastSetAt: LocalDateTime? = null,
  val activeToLastSetBy: String? = null,
  val activeToLastSetByDisplayName: String? = null,
  val prisonCodeWhenCreated: String? = null,
) {
  fun toAlert() =
    Alert(
      offenderNo = this.prisonNumber,
      type = this.alertCode.alertTypeCode,
      typeDescription = this.alertCode.alertTypeDescription,
      code = this.alertCode.code,
      codeDescription = this.alertCode.description,
      comment = this.description,
      dateCreated = this.activeFrom,
      dateExpired = this.activeTo,
      expired = this.activeTo?.isBefore(LocalDate.now().plusDays(1)) ?: false,
      active = this.isActive,
    )
}
