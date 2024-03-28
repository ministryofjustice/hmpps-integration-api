package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps
import java.time.LocalDate

data class HomeDetentionCurfewDate(
  val actualDate: LocalDate? = null,
  val eligibilityCalculatedDate: LocalDate? = null,
  val eligibilityDate: LocalDate? = null,
  val eligibilityOverrideDate: LocalDate? = null,
  val endDate: LocalDate? = null,
)
