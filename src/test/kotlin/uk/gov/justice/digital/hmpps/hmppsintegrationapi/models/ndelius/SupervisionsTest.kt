package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence

class SupervisionsTest : DescribeSpec(
  {
    describe("#toOffence") {
      it("maps one-to-one attributes to integration API attributes") {
        val supervisions = Supervisions(
          supervisions = listOf(
            Supervision(mainOffence = MainOffence(description = "foobar")),
            Supervision(mainOffence = MainOffence(description = "barbaz")),
          ),
        )

        val integrationApiOffences = supervisions.supervisions.map { it.toOffence() }

        integrationApiOffences.shouldBe(
          listOf(
            Offence(description = "foobar"),
            Offence(description = "barbaz"),
          ),
        )
      }
    }
  },
)
