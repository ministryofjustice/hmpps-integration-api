package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@Configuration
class ClockConfiguration {
  @Bean
  fun clock(): Clock = Clock.systemDefaultZone()
}

fun fixedClock(): Clock = Clock.fixed(Clock.systemDefaultZone().instant(), ZoneId.systemDefault())

fun ukDateTimeToInstant(dateTime: String?): String? {
  if (dateTime.isNullOrEmpty()) {
    return null
  }

  val local = LocalDateTime.parse(dateTime)
  val instant = local.atZone(ZoneId.of("Europe/London")).toInstant()
  return instant.toString()
}
