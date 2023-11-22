package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

import java.time.LocalDate

data class LatestSentenceKeyDatesAndAdjustments(
  val adjustments: SentenceAdjustment? = null,
  val automaticRelease: SentenceKeyDate? = null,
  val conditionalRelease: SentenceKeyDate? = null,
  val dtoPostRecallRelease: SentenceKeyDate? = null,
  val earlyTerm: SentenceKeyDate? = null,
  val homeDetentionCurfew: HomeDetentionCurfewDate? = null,
  val lateTerm: SentenceKeyDate? = null,
  val licenceExpiry: SentenceKeyDate? = null,
  val midTerm: SentenceKeyDate? = null,
  val nonDto: NonDtoDate? = null,
  val nonParole: SentenceKeyDate? = null,
  val paroleEligibility: SentenceKeyDate? = null,
  val postRecallRelease: SentenceKeyDate? = null,
  val release: ReleaseDate? = null,
  val sentence: SentenceDate? = null,
  val actualParoleDate: LocalDate? = null,
  val earlyRemovalSchemeEligibilityDate: LocalDate? = null,
  val releaseOnTemporaryLicenceDate: LocalDate? = null,
  val tariffDate: LocalDate? = null,
  val tariffEarlyRemovalSchemeEligibilityDate: LocalDate? = null,
)
