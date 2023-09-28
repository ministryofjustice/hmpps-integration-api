package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Needs as ArnNeeds

class NeedsTest : DescribeSpec(
  {
    describe("#toNeeds") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnNeeds = ArnNeeds(
          assessedOn = LocalDateTime.parse("2000-11-27T10:15:41"),
        )

        val integrationApiNeeds = arnNeeds.toNeeds()

        integrationApiNeeds.assessedOn.shouldBe(arnNeeds.assessedOn)
      }
    }
  },
)
