package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate

class SupervisionsTest : DescribeSpec(
  {
    describe("#toOffence") {
      it("maps one-to-one attributes to integration API attributes") {
        val supervisions = Supervisions(
          supervisions = listOf(
            Supervision(
              mainOffence = MainOffence(
                description = "foobar",
                code = "05800",
              ),
              courtAppearances = listOf(
                CourtAppearance(
                  date = "2009-07-07T00:00:00+01:00",
                ),
              ),
            ),
            Supervision(
              mainOffence = MainOffence(
                description = "barbaz",
                code = "05800",
              ),
              courtAppearances = listOf(
                CourtAppearance(date = "2010-07-07T00:00:00+01:00"),
              ),
            ),
          ),
        )

        val integrationApiOffences = supervisions.supervisions.map { it.toOffence() }

        integrationApiOffences.shouldBe(
          listOf(
            Offence(
              description = "foobar",
              hoCode = "05800",
              courtDates = listOf(LocalDate.parse("2009-07-07")),
            ),
            Offence(
              description = "barbaz",
              hoCode = "05800",
              courtDates = listOf(LocalDate.parse("2010-07-07")),
            ),
          ),
        )
      }
    }
  },
)
