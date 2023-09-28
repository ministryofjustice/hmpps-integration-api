package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.NotIdentifiedNeeds as ArnNotIdentifiedNeeds
class NotIdentifiedNeedsTest : DescribeSpec(
  {
    describe("#toNotIdentifiedNeeds") {
      it("maps one-to-one attributes to integration API attributes") {
        val arnNotIdentifiedNeeds = ArnNotIdentifiedNeeds(
          section = "ALCOHOL_MISUSE",
        )

        val integrationApiNotIdentifiedNeeds = arnNotIdentifiedNeeds.toNotIdentifiedNeeds()

        integrationApiNotIdentifiedNeeds.type.shouldBe(arnNotIdentifiedNeeds.section)
      }
    }
  },
)
