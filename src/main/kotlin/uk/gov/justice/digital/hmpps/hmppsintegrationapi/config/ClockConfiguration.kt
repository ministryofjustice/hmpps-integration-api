package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

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

fun ukDateTimeToInstant(
  date: String?,
  time: String?,
): Instant? {
  if (date.isNullOrEmpty() || time.isNullOrEmpty()) {
    return null
  }

  return try {
    val local = LocalDateTime.parse("${date}T$time")
    return local.atZone(ZoneId.of("Europe/London")).toInstant()
  } catch (e: DateTimeParseException) {
    null
  }
}
