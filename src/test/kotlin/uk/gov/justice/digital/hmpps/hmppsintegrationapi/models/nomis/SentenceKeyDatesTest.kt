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
          conditionalReleaseDate = LocalDate.parse("2022-04-01"),
          conditionalReleaseOverrideDate = LocalDate.parse("2022-04-01"),
          dtoPostRecallReleaseDate = LocalDate.parse("2022-05-01"),
          dtoPostRecallReleaseDateOverride = LocalDate.parse("2022-05-01"),
          earlyTermDate = LocalDate.parse("2021-04-01"),
          etdOverrideDate = LocalDate.parse("2021-04-01"),
          etdCalculatedDate = LocalDate.parse("2021-04-01"),
        )

        val sentenceKeyDates = sentenceKeyDatesFromNomis.toSentenceKeyDates()

        sentenceKeyDates.automaticRelease.date.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseDate)
        sentenceKeyDates.automaticRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.automaticReleaseOverrideDate)

        sentenceKeyDates.conditionalRelease.date.shouldBe(sentenceKeyDatesFromNomis.conditionalReleaseDate)
        sentenceKeyDates.conditionalRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.conditionalReleaseOverrideDate)

        sentenceKeyDates.dtoPostRecallRelease.date.shouldBe(sentenceKeyDatesFromNomis.dtoPostRecallReleaseDate)
        sentenceKeyDates.dtoPostRecallRelease.overrideDate.shouldBe(sentenceKeyDatesFromNomis.dtoPostRecallReleaseDateOverride)

        sentenceKeyDates.earlyTerm.date.shouldBe(sentenceKeyDatesFromNomis.earlyTermDate)
        sentenceKeyDates.earlyTerm.overrideDate.shouldBe(sentenceKeyDatesFromNomis.etdOverrideDate)
        sentenceKeyDates.earlyTerm.calculatedDate.shouldBe(sentenceKeyDatesFromNomis.etdCalculatedDate)
      }
    }
  },
)
