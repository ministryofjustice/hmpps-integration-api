package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

import java.time.LocalDate

data class SentenceKeyDates(
  val automaticRelease: SentenceKeyDate = SentenceKeyDate(),
  val conditionalRelease: SentenceKeyDate = SentenceKeyDate(),
  val dtoPostRecallRelease: SentenceKeyDate = SentenceKeyDate(),
  val earlyTerm: SentenceKeyDateWithCalculatedDate = SentenceKeyDateWithCalculatedDate(),
  val homeDetentionCurfew: HomeDetentionCurfewDate = HomeDetentionCurfewDate(),
  val lateTerm: SentenceKeyDateWithCalculatedDate = SentenceKeyDateWithCalculatedDate(),
  val licenceExpiry: SentenceKeyDateWithCalculatedDate = SentenceKeyDateWithCalculatedDate(),
  val midTerm: SentenceKeyDateWithCalculatedDate = SentenceKeyDateWithCalculatedDate(),
  val nonDto: NonDtoDate = NonDtoDate(),
  val nonParole: SentenceKeyDate = SentenceKeyDate(),
  val paroleEligibility: SentenceKeyDateWithCalculatedDate = SentenceKeyDateWithCalculatedDate(),
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
