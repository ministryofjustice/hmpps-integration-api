package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDateTime

data class ImageMetadata(
  val id: Long,
  val active: Boolean,
  val captureDateTime: LocalDateTime,
  val view: String,
  val orientation: String,
  val type: String,
)
