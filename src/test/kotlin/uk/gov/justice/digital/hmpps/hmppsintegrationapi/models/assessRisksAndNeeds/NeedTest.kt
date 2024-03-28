package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class NeedTest : DescribeSpec(
  {
    describe("#toNeed") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnNeed =
          ArnNeed(
            section = "ACCOMMODATION",
          )

        val integrationApiNeed = arnNeed.toNeed()

        integrationApiNeed.type.shouldBe(arnNeed.section)
      }
    }
  },
)
