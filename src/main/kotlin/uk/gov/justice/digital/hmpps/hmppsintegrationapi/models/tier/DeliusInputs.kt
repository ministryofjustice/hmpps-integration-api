package uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.tier

import java.math.BigDecimal
import java.time.LocalDate

data class DeliusInputs(
  val isFemale: Boolean? = null,
  val rsrScore: BigDecimal? = null,
  val ogrsScore: Int? = null,
  val hasNoMandate: Boolean? = null,
  val registrations: Registrations? = null,
  val previousEnforcementActivity: Boolean? = null,
  val latestReleaseDate: LocalDate? = null,
  val hasActiveEvent: Boolean? = null,
  val latestSentencingAct2026ExcludedOffenceDate: LocalDate? = null,
  val latestChildSexualExploitationSentenceDate: LocalDate? = null,
)
