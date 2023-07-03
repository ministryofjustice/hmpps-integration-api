package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class ImageDetailTest : DescribeSpec(
  {
    describe("#toImageMetadata") {
      it("maps one-to-one attributes to integration API attributes") {
        val imageDetail = ImageDetail(
          imageId = 1,
          active = true,
          captureDateTime = LocalDateTime.now(),
          imageView = "View",
          imageOrientation = "Orientation",
          imageType = "SomeType"
        )
        val imageMetaData = imageDetail.toImageMetadata()

        imageMetaData.id.shouldBe(imageDetail.imageId)
        imageMetaData.active.shouldBe(imageDetail.active)
        imageMetaData.captureDateTime.shouldBe(imageDetail.captureDateTime)
        imageMetaData.view.shouldBe(imageDetail.imageView)
        imageMetaData.orientation.shouldBe(imageDetail.imageOrientation)
        imageMetaData.type.shouldBe(imageDetail.imageType)
      }
    }
  },
)
