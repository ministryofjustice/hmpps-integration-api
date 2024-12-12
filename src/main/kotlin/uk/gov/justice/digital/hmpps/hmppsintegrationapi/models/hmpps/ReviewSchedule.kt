package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedules(
  val reviewSchedules: List<ReviewSchedule>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.USE_DEFAULTS)
data class ReviewSchedule(
  val reference: String,
  val reviewDateFrom: LocalDate,
  val reviewDateTo: LocalDate,
  val status: String,
  val calculationRule: String,
  val createdBy: String,
  val createdByDisplayName: String,
  val createdAt: Instant,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedByDisplayName: String,
  val updatedAt: Instant,
  val updatedAtPrison: String,
  val version: Int,
)
