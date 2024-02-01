package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps

data class SentenceTerm(
  val years: Int? = null,
  val months: Int? = null,
  val weeks: Int? = null,
  val days: Int? = null,
  val hours: Int? = null,
  val prisonTermCode: String? = null,
)
