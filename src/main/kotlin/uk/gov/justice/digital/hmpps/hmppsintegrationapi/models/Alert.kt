package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class Alert(
  val offenderNo: String? = null,
  val type: String? = null,
  val typeDescription: String? = null,
  val code: String? = null,
  val codeDescription: String? = null,
  val comment: String? = null,
  val dateCreated: LocalDate? = null,
  val dateExpired: LocalDate? = null,
  val expired: Boolean? = null,
  val active: Boolean? = null,
)
