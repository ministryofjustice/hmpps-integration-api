package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Sentence as NomisSentence

class PersonSentencesTest : DescribeSpec(
  {
    describe("#toOffence") {
      it("maps one-to-one attributes to integration API attributes") {
        val nomisSentence = NomisSentence(
          startDate = LocalDate.parse("2022-02-02"),
          days = 19,
          weeks = 2,
          months = 6,
          years = 10,
          fineAmount = 10000.00,
          lifeSentence = true,
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.days.shouldBe(nomisSentence.days)
        integrationApiSentence.weeks.shouldBe(nomisSentence.weeks)
        integrationApiSentence.months.shouldBe(nomisSentence.months)
        integrationApiSentence.years.shouldBe(nomisSentence.years)
        integrationApiSentence.fineAmount.shouldBe(nomisSentence.fineAmount)
        integrationApiSentence.isLifeSentence.shouldBe(nomisSentence.lifeSentence)
      }

      it("deals with NULL values") {
        val integrationApiSentence = Sentence(isLifeSentence = true)

        integrationApiSentence.startDate.shouldBeNull()
        integrationApiSentence.days.shouldBeNull()
        integrationApiSentence.weeks.shouldBeNull()
        integrationApiSentence.months.shouldBeNull()
        integrationApiSentence.years.shouldBeNull()
        integrationApiSentence.fineAmount.shouldBeNull()
        integrationApiSentence.isLifeSentence.shouldBe(true)
      }
    }
  },
)
