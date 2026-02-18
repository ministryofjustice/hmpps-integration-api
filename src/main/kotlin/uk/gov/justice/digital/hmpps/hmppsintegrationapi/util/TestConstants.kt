package uk.gov.justice.digital.hmpps.hmppsintegrationapi.util

import java.time.Clock
import java.time.ZoneId

object TestConstants {
  const val DEFAULT_CRN = "A123123"
  val FIXED_CLOCK: Clock = Clock.fixed(Clock.systemDefaultZone().instant(), ZoneId.systemDefault())
}
