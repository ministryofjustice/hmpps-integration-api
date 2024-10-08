package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LicenceTest : DescribeSpec(
  {
    describe("#toAddress") {
      it("maps one-to-one attributes to integration API attributes") {
        val cvlLcence =
          CvlLicenceSummary(
            id = "MockId",
            prisonNumber = "1140484",
          )

        val integrationApiLicence = cvlLcence.toLicence()

        integrationApiLicence.offenderNumber.shouldBe(cvlLcence.prisonNumber)
      }
    }
  },
)
