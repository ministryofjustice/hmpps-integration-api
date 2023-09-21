package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.assessRisksAndNeeds.Risks as ArnRisks

class RisksTest : DescribeSpec(
  {
    describe("#toRisks") {
      it("maps one-to-one attributes to Integration API attributes") {
        val arnRisks = ArnRisks(
          assessedOn = LocalDateTime.now(),
        )

        val integrationApiRisks = arnRisks.toRisks()

        integrationApiRisks.assessedOn.shouldBe(
          arnRisks.assessedOn,
        )
      }
    }
  },
)
