package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class ImageMetadata(
  val id: Integer,
  val captureDate: LocalDate?
)
