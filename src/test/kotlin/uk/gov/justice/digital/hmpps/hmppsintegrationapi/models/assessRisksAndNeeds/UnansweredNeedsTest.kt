package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.UnansweredNeeds as ArnUnansweredNeeds
class UnansweredNeedsTest : DescribeSpec(
  {
    describe("#toUnansweredNeeds") {
      it("maps one-to-one attributes to integration API attributes") {
        val arnUnansweredNeeds = ArnUnansweredNeeds(
          section = "ACCOMMODATION",
        )

        val integrationApiUnansweredNeeds = arnUnansweredNeeds.toUnansweredNeeds()

        integrationApiUnansweredNeeds.type.shouldBe(arnUnansweredNeeds.section)
      }
    }
  },
)
