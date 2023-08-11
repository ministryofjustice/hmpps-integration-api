package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Sentence as NDeliusSentence


class SupervisionsTest : DescribeSpec(
  {
    describe("#toOffences") {
      describe("When there are additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions = Supervisions(
            supervisions = listOf(
              Supervision(
                mainOffence = MainOffence(description = "foobar", code = "05800", date = "2000-01-02"),
                additionalOffences = listOf(AdditionalOffence(description = "additionalFoo", code = "12345", date = "2001-01-01")),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                mainOffence = MainOffence(description = "barbaz", code = "05800", date = "2003-03-03"),
                additionalOffences = listOf(AdditionalOffence(description = "additionalFoo2", code = "98765", date = "2001-02-02")),
                courtAppearances = listOf(CourtAppearance(date = "2010-07-07T00:00:00+01:00")),
              ),
            ),
          )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2000-01-02"),
              ),
              Offence(
                description = "additionalFoo",
                hoCode = "12345",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2001-01-01"),
              ),
              Offence(
                description = "barbaz",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2003-03-03"),
              ),
              Offence(
                description = "additionalFoo2",
                hoCode = "98765",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2001-02-02"),
              ),
            ),
          )
        }
      }

      describe("When there are no additional offences") {
        it("maps one-to-one attributes to integration API attributes") {
          val supervisions = Supervisions(
            supervisions = listOf(
              Supervision(
                mainOffence = MainOffence(description = "foobar", code = "05800", date = "2019-09-09"),
                additionalOffences = emptyList(),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                mainOffence = MainOffence(description = "barbaz", code = "05800", date = "2020-02-03"),
                additionalOffences = emptyList(),
                courtAppearances = listOf(CourtAppearance(date = "2010-07-07T00:00:00+01:00")),
              ),
            ),
          )

          val integrationApiOffences = supervisions.supervisions.flatMap { it.toOffences() }

          integrationApiOffences.shouldBe(
            listOf(
              Offence(
                description = "foobar",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2009-07-07")),
                startDate = LocalDate.parse("2019-09-09"),
              ),
              Offence(
                description = "barbaz",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2010-07-07")),
                startDate = LocalDate.parse("2020-02-03"),
              ),
            ),
          )
        }
      }
    }
    describe("#toSentence") {
      it("maps one-to-one attributes to integration API sentence attributes") {
        val supervisions = Supervisions(
          listOf(
            Supervision(active = true, sentence = NDeliusSentence(date = "2009-07-07")),
            Supervision(active = false, sentence = NDeliusSentence(date = "2010-07-07")),
          ),
        )

        val integrationApiSentences = supervisions.supervisions.map { it.toSentence() }

        integrationApiSentences.shouldBe(
          listOf(
            IntegrationApiSentence(dateOfSentencing = LocalDate.parse("2009-07-07"), isActive = true),
            IntegrationApiSentence(dateOfSentencing = LocalDate.parse("2010-07-07"), isActive = false),
          ),
        )
      }
    }
  },
)
