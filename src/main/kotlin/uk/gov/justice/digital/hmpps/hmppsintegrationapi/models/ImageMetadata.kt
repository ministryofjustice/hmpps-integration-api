package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class ImageMetadata(
  val imageId : Long,
  val captureDate: LocalDate,
  val view: String,
  val orientation: String,
  val type: String,
)
