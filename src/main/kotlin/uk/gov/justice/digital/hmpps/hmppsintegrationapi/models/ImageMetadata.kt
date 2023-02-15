package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class ImageMetadata(
  val id: Int,
  val captureDate: LocalDate?
)
