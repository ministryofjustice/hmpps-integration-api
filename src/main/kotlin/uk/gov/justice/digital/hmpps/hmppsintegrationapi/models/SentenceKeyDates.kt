package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class SentenceKeyDates(
  val automaticRelease: SentenceKeyDate = SentenceKeyDate(),
  val conditionalRelease: SentenceKeyDate = SentenceKeyDate(),
  val dtoPostRecallRelease: SentenceKeyDate = SentenceKeyDate(),
  val earlyTerm: SentenceKeyDate = SentenceKeyDate(),
  val homeDetentionCurfew: HomeDetentionCurfewDate = HomeDetentionCurfewDate(),
  val lateTerm: SentenceKeyDate = SentenceKeyDate(),
  val licenceExpiry: SentenceKeyDate = SentenceKeyDate(),
  val midTerm: SentenceKeyDate = SentenceKeyDate(),
  val nonDto: NonDtoDate = NonDtoDate(),
  val nonParole: SentenceKeyDate = SentenceKeyDate(),
  val paroleEligibility: SentenceKeyDate = SentenceKeyDate(),
  val postRecallRelease: SentenceKeyDate = SentenceKeyDate(),
  val release: ReleaseDate = ReleaseDate(),
  val sentence: SentenceDate = SentenceDate(),
  val topupSupervision: TopupSupervision = TopupSupervision(),
  val actualParoleDate: LocalDate? = null,
  val earlyRemovalSchemeEligibilityDate: LocalDate? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val tariffDate: LocalDate? = null,
  val tariffEarlyRemovalSchemeEligibilityDate: LocalDate? = null,
)
