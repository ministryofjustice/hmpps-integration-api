package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

data class Alert(
  @Schema(description = "Offender unique reference", example = "Z1234ZZ")
  val offenderNo: String? = null,
  @Schema(description = "Alert type", example = "X")
  val type: String? = null,
  @Schema(description = "Alert type description", example = "Security")
  val typeDescription: String? = null,
  @Schema(description = "Alert code", example = "PO")
  val code: String? = null,
  @Schema(description = "Alert code description", example = "MAPPA Nominal")
  val codeDescription: String? = null,
  @Schema(description = "Alert comment", example = "Professional lock pick")
  val comment: String? = null,
  @Schema(description = "Date of the alert, which might differ from the date it was created", example = "2014-09-23")
  val dateCreated: LocalDate? = null,
  @Schema(description = "Date that the alert expires", example = "2015-09-23")
  val dateExpired: LocalDate? = null,
  @Schema(description = "Whether the alert has expired", example = "true")
  val expired: Boolean? = null,
  @Schema(description = "Whether the alert is active", example = "false")
  val active: Boolean? = null,
)
