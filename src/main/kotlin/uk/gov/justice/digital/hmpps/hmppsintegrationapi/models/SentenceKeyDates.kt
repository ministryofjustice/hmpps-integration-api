package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models

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
)
