package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import java.time.LocalDate

class ImageDetail(
  val imageId: Int,
  val captureDate: LocalDate
  ) {
  fun toImageMetadata() : ImageMetadata = ImageMetadata(
    this.imageId,
    this.captureDate
  )
}