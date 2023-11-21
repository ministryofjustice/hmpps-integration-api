package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

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
)
