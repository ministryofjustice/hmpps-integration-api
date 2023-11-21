package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceKeyDates as SentenceKeyDatesFromNomis
class SentenceKeyDatesTest : DescribeSpec(
  {
    describe("#toSentenceKeyDates") {
      it("maps one-to-one attributes to integration API attributes") {
        val sentenceKeyDatesFromNomis = SentenceKeyDatesFromNomis(
          automaticReleaseDate = LocalDate.parse("2022-03-01"),
          automaticReleaseOverrideDate = LocalDate.parse("2022-03-01"),
        )

        val sentenceKeyDates = sentenceKeyDatesFromNomis.toSentenceKeyDates()

        sentenceKeyDates.automaticRelease.date.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseDate)
        sentenceKeyDates.automaticRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseOverrideDate)
      }
    }
  },
)
