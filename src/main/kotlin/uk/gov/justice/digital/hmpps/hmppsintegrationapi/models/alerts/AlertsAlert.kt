package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.alerts

import java.time.LocalDateTime

data class AlertsAlert(
  val alertUid: String,
  val prisonNumber: String,
  val alertCode: AlertsAlertCode,
  val description: String? = null,
  val authorisedBy: String? = null,
  val activeFrom: String,
  val activeTo: String? = null,
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
)
