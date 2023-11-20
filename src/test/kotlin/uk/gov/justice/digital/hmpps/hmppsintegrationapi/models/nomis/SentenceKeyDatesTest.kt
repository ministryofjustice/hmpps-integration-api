package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceKeyDate as SentenceKeyDateFromNomis
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.nomis.SentenceKeyDates as SentenceKeyDatesFromNomis
class SentenceKeyDatesTest : DescribeSpec(
  {
    describe("#toSentenceKeyDates") {
      it("maps one-to-one attributes to integration API attributes") {
        val sentenceKeyDatesFromNomis = SentenceKeyDatesFromNomis(
          automaticReleaseDate = SentenceKeyDateFromNomis(date = LocalDate.parse("2022-03-01"), overrideDate = LocalDate.parse("2022-03-01")),
        )

        val sentenceKeyDates = sentenceKeyDatesFromNomis.toSentenceKeyDates()

        sentenceKeyDates.automaticRelease.date.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseDate.date)
        sentenceKeyDates.automaticRelease.overrideDate.shouldBe((sentenceKeyDatesFromNomis.automaticReleaseDate.overrideDate))
      }
    }
  },
)
