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
          sentenceDate = LocalDate.parse("2022-02-02"),
        )

        val integrationApiSentence = nomisSentence.toSentence()
        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
      }

      it("deals with NULL values") {
        val integrationApiSentence = Sentence()
        integrationApiSentence.dateOfSentencing.shouldBeNull()
      }
    }
  },
)
