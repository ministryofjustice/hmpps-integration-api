package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Sentence as IntegrationApiSentence
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.Sentence as NDeliusSentence

class SentenceTest : DescribeSpec(
  {
    describe("#toSentence") {
      it("maps one-to-one attributes to integration API sentence attributes") {
        val supervisions = Supervisions(
          listOf(
            Supervision(sentence = NDeliusSentence(date = "2009-07-07", active = true)),
            Supervision(sentence = NDeliusSentence(date = "2010-07-07", active = false)),
          ),
        )

        val integrationApiSentences = supervisions.supervisions.map { it.sentence.toSentence() }

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
