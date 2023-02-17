package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ImageMetadata
import java.time.LocalDate

class ImageDetail(
  val captureDate: LocalDate,
  val imageView: String,
  val imageOrientation: String,
  val imageType: String,
) {
  fun toImageMetadata(): ImageMetadata = ImageMetadata(
    captureDate = this.captureDate,
    view = this.imageView,
    orientation = this.imageOrientation,
    type = this.imageType,
  )
}
