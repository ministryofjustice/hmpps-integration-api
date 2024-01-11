package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ImageMetadata
import java.time.LocalDateTime

class ImageDetail(
  val imageId: Long,
  val active: Boolean,
  val captureDateTime: LocalDateTime,
  val imageView: String,
  val imageOrientation: String,
  val imageType: String,
) {
  fun toImageMetadata(): ImageMetadata = ImageMetadata(
    id = this.imageId,
    active = this.active,
    captureDateTime = this.captureDateTime,
    view = this.imageView,
    orientation = this.imageOrientation,
    type = this.imageType,
  )
}
