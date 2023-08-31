package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.generateTestSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Offence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
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
                custodial = true,
                mainOffence = MainOffence(description = "foobar", code = "05800", date = "2000-01-02"),
                additionalOffences = listOf(AdditionalOffence(description = "additionalFoo", code = "12345", date = "2001-01-01")),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                custodial = true,
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
                custodial = true,
                mainOffence = MainOffence(description = "foobar", code = "05800", date = "2019-09-09"),
                additionalOffences = emptyList(),
                courtAppearances = listOf(CourtAppearance(date = "2009-07-07T00:00:00+01:00")),
              ),
              Supervision(
                custodial = true,
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
            Supervision(active = true, custodial = true, sentence = NDeliusSentence(date = "2009-07-07", description = "CJA - Community Order")),
            Supervision(active = false, custodial = true, sentence = NDeliusSentence(date = "2010-07-07", description = "CJA - Suspended Sentence Order")),
          ),
        )

        val integrationApiSentences = supervisions.supervisions.map { it.toSentence() }

        integrationApiSentences.shouldBe(
          listOf(
            generateTestSentence(
              dataSource = UpstreamApi.NDELIUS,
              dateOfSentencing = LocalDate.parse("2009-07-07"),
              description = "CJA - Community Order",
              terms = listOf(Term()),
            ),
            generateTestSentence(
              dataSource = UpstreamApi.NDELIUS,
              dateOfSentencing = LocalDate.parse("2010-07-07"),
              isActive = false,
              description = "CJA - Suspended Sentence Order",
              terms = listOf(Term()),
            ),
          ),
        )
      }

      xit("maps nDelius sentence length to Integration API terms") {
        val supervisions = Supervisions(
          listOf(
            Supervision(active = true, custodial = true, sentence = NDeliusSentence(date = "2009-07-07", description = "CJA - Community Order")),
            Supervision(active = false, custodial = true, sentence = NDeliusSentence(date = "2010-07-07", description = "CJA - Suspended Sentence Order")),
          ),
        )

        val integrationApiSentences = supervisions.supervisions.map { it.toSentence() }

        integrationApiSentences[0].terms.shouldBe(listOf(Term()))
        integrationApiSentences[1].terms.shouldBe(listOf(Term()))
      }

      it("deals with NULL values") {
        val supervisions = Supervisions(
          listOf(
            Supervision(custodial = true),
            Supervision(custodial = true),
          ),
        )

        val integrationApiSentences = supervisions.supervisions.map { it.toSentence() }

        for (integrationApiSentence in integrationApiSentences) {
          integrationApiSentence.dateOfSentencing.shouldBeNull()
          integrationApiSentence.isActive.shouldBeNull()
          integrationApiSentence.description.shouldBeNull()
          integrationApiSentence.terms.first().prisonTermCode.shouldBeNull()
          integrationApiSentence.terms.shouldBe(listOf(Term()))
        }
      }
    }
  },
)
