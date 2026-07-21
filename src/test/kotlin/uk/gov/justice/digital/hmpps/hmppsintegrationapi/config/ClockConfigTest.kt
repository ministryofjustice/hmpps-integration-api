package uk.gov.justice.digital.hmpps.hmppsintegrationapi.config

import java.time.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClockConfigTest {
  @Test
  fun `clock bean returns a system default clock`() {
    val clockConfig = ClockConfiguration()
    val clock = clockConfig.clock()

    assertNotNull(clock)
    assertEquals(Clock.systemDefaultZone().zone, clock.zone)
  }

  @Test
  fun `fixedClock returns a clock that does not advance time`() {
    val clock = fixedClock()

    val time1 = clock.instant()
    Thread.sleep(10)
    val time2 = clock.instant()

    assertEquals(time1, time2)
  }

  @Test
  fun `localDateTimeToInstant converts winter date correctly`() {
    val inputDate = "2023-12-08T15:50:37"
    val expectedInstant = "2023-12-08T15:50:37Z"

    val result = localDateTimeToInstant(inputDate)

    assertEquals(expectedInstant, result)
  }

  @Test
  fun `localDateTimeToInstant converts summer date correctly handling BST`() {
    val inputDate = "2023-07-08T15:50:37"
    // The resulting instant should be 1 hour behind local time in July
    val expectedInstant = "2023-07-08T14:50:37Z"

    val result = localDateTimeToInstant(inputDate)

    assertEquals(expectedInstant, result)
  }
}
