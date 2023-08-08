package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Sentence as NDeliusSentence

class SupervisionsTest : DescribeSpec(
  {
    describe("#toOffences") {
      describe("When there are additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions = Supervisions(
            supervisions = listOf(
              Supervision(
                mainOffence = MainOffence(description = "foobar", code = "05800"),
                additionalOffences = listOf(AdditionalOffence(description = "additionalFoo", code = "12345")),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                mainOffence = MainOffence(description = "barbaz", code = "05800"),
                additionalOffences = listOf(AdditionalOffence(description = "additionalFoo2", code = "98765")),
                courtAppearances = listOf(CourtAppearance(date = "2010-07-07T00:00:00+01:00")),
              ),
            ),
          )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(description = "foobar", hoCode = "05800", courtDates = listOf(LocalDate.parse("2009-07-07"))),
              Offence(description = "additionalFoo", hoCode = "12345", courtDates = listOf(LocalDate.parse("2009-07-07"))),
              Offence(description = "barbaz", hoCode = "05800", courtDates = listOf(LocalDate.parse("2010-07-07"))),
              Offence(description = "additionalFoo2", hoCode = "98765", courtDates = listOf(LocalDate.parse("2010-07-07"))),
            ),
          )
        }
      }

      describe("When there are no additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions = Supervisions(
            supervisions = listOf(
              Supervision(
                mainOffence = MainOffence(description = "foobar", code = "05800"),
                additionalOffences = emptyList(),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                mainOffence = MainOffence(description = "barbaz", code = "05800"),
                additionalOffences = emptyList(),
                courtAppearances = listOf(CourtAppearance(date = "2010-07-07T00:00:00+01:00")),
              ),
            ),
          )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(description = "foobar", hoCode = "05800", courtDates = listOf(LocalDate.parse("2009-07-07"))),
              Offence(description = "barbaz", hoCode = "05800", courtDates = listOf(LocalDate.parse("2010-07-07"))),
            ),
          )
        }
      }
    }

    describe("#toSentence") {
      it("maps one-to-one attributes to integration API sentence attributes") {
        val supervisions = Supervisions(
          supervisions = listOf(
            Supervision(sentence = NDeliusSentence(date = "01-01-1900")),
            Supervision(sentence = NDeliusSentence(date = "01-01-2000")),
          ),
        )

        val integrationApiSentence = supervisions.supervisions.map { it.sentence.toSentence() }

        integrationApiSentence.shouldBe(
          listOf(
            Sentence(dateOfSentencing = LocalDate.parse("01-01-1900")),
            Sentence(dateOfSentencing = LocalDate.parse("01-01-2000")),
          ),
        )
      }
    }
  },
)
