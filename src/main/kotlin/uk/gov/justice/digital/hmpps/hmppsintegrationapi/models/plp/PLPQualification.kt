package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.plp

import java.time.LocalDateTime

data class PLPQualification(
  val reference: String,
  val subject: String,
  val level: String,
  val grade: String,
  val createdBy: String,
  val createdAt: LocalDateTime,
  val createdAtPrison: String,
  val updatedBy: String,
  val updatedAt: LocalDateTime,
  val updatedAtPrison: String,
)
