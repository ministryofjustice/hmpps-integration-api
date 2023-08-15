package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Term as IntegrationApiTerm
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Sentence as NomisSentence
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.Term as NomisTerm

class PersonSentencesTest : DescribeSpec(
  {
    describe("#toSentence") {
      it("maps one-to-one attributes to integration API attributes") {
        val nomisSentence = NomisSentence(
          sentenceDate = LocalDate.parse("2022-02-02"),
          sentenceStatus = "A",
          terms = listOf(
            NomisTerm(
              years = 3,
              months = 4,
              weeks = 0,
              days = 2,
            ),
            NomisTerm(
              years = 7,
              months = 3,
              weeks = 4,
              days = 0,
            ),
          ),

        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(true)
        integrationApiSentence.terms.shouldBe(
          listOf(
            IntegrationApiTerm(
              years = 3,
              months = 4,
              weeks = 0,
              days = 2,
            ),
            IntegrationApiTerm(
              years = 7,
              months = 3,
              weeks = 4,
              days = 0,
            ),
          ),
        )
      }

      it("sentenceStatusToBoolean correctly maps 'I' to false") {
        val nomisSentence = NomisSentence(
          sentenceDate = null,
          sentenceStatus = "I",
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(false)
      }

      it("sentenceStatusToBoolean correctly maps anything other than 'A' or 'I' to null") {
        val nomisSentence = NomisSentence(
          sentenceDate = null,
          sentenceStatus = "X",
        )

        val integrationApiSentence = nomisSentence.toSentence()

        integrationApiSentence.dateOfSentencing.shouldBe(nomisSentence.sentenceDate)
        integrationApiSentence.isActive.shouldBe(null)
      }

      it("deals with NULL values") {
        val integrationApiSentence = IntegrationApiSentence()
        integrationApiSentence.dateOfSentencing.shouldBeNull()
        integrationApiSentence.isActive.shouldBeNull()
        integrationApiSentence.terms.shouldBe(listOf(IntegrationApiTerm()))
      }
    }
  },
)
