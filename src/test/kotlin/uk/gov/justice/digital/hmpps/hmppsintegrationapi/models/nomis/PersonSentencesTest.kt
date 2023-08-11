package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Sentence as NomisSentence

class PersonSentencesTest : DescribeSpec(
  {
    describe("#toSentence") {
      it("maps one-to-one attributes to integration API attributes") {
        val nomisSentence = NomisSentence(
          sentenceDate = LocalDate.parse("2022-02-02"),
          sentenceStatus = "A",
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(true)
      }

      it("toIsActive correctly maps 'I' to false") {
        val nomisSentence = NomisSentence(
          sentenceDate = null,
          sentenceStatus = "I",
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(false)
      }

      it("toIsActive correctly maps anything other than 'A' or 'I' to null") {
        val nomisSentence = NomisSentence(
          sentenceDate = null,
          sentenceStatus = "X",
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(null)
      }

      it("deals with NULL values") {
        val integrationApiSentence = Sentence()
        integrationApiSentence.dateOfSentencing.shouldBeNull()
        integrationApiSentence.isActive.shouldBeNull()
      }
    }
  },
)
