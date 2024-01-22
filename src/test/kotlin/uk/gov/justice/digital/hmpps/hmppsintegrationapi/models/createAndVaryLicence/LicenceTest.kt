package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.createAndVaryLicence
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class LicenceTest : DescribeSpec(
  {
    describe("#toAddress") {
      it("maps one-to-one attributes to integration API attributes") {
        val cvlLcense = CvlLicence(
          prisonNumber = "1140484",
        )

        val integrationApiLicense = cvlLcense.toLicence()

        integrationApiLicense.offenderNumber.shouldBe(cvlLcense.prisonNumber)
      }
    }
  },
)
