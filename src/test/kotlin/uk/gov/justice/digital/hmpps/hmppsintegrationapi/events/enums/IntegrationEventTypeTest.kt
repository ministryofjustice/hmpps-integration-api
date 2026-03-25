package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class IntegrationEventTypeTest {
  /**
   * This test will fail when there is unexpected enum value(s)
   *
   * The enum [IntegrationEventType] is persisted to DB, and please consider these while making changes
   * - Adding new value is safe
   * - Renaming current value is unsafe
   * - Removing current value is unsafe
   *
   * For renaming or removal of current value, please provide transition period
   * i) Mark deprecated
   * ii) Wait for transition period over
   * iii) Finally remove it
   */
  @Test
  fun `should have all event types expected`() {
    val expectedEventTypesHashCode = -591363030

    val actualEventTypesHashCode =
      IntegrationEventType.entries
        .map { it.name }
        .sorted()
        .joinToString(",") { "\"$it\"" }
        .hashCode()

    assertEquals(expectedEventTypesHashCode, actualEventTypesHashCode)
  }
}
