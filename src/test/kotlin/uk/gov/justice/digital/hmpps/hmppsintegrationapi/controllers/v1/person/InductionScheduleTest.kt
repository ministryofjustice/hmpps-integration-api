package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.InductionSchedule
import java.time.Instant
import java.time.LocalDate

class InductionScheduleTest {
  private val objectMapper =
    ObjectMapper().apply {
      // Register the custom deserializer
      registerModule(
        com.fasterxml.jackson.module.kotlin.KotlinModule.Builder().build(),
      )
    }

  @Test
  fun `should deserialize JSON into InductionSchedule object`() {
    // Given
    val json = """
        {
            "prisonNumber": "A1234BC",
            "deadlineDate": "2023-09-01",
            "scheduleStatus": "SCHEDULED",
            "scheduleCalculationRule": "NEW_PRISON_ADMISSION",
            "updatedByDisplayName": "John Smith",
            "updatedAt": "2023-06-19T09:39:44Z",
            "inductionPerformedBy": "Fred Jones",
            "inductionPerformedAt": "2023-06-30"
        }
        """

    // When
    val result = objectMapper.readValue(json, InductionSchedule::class.java)

    // Then
    assertEquals("A1234BC", result.nomisNumber)
    assertEquals(LocalDate.of(2023, 9, 1), result.deadlineDate)
    assertEquals("SCHEDULED", result.status)
    assertEquals("NEW_PRISON_ADMISSION", result.calculationRule)
    assertEquals("John Smith", result.systemUpdatedBy)
    assertEquals(Instant.parse("2023-06-19T09:39:44Z"), result.systemUpdatedAt)
    assertEquals("Fred Jones", result.inductionPerformedBy)
    assertEquals(LocalDate.of(2023, 6, 30), result.inductionPerformedAt)
  }

  @Test
  fun `should handle missing fields in JSON`() {
    // Given
    val json = """
        {
            "prisonNumber": "A1234BC"
        }
        """

    // When
    val result = objectMapper.readValue(json, InductionSchedule::class.java)

    // Then
    assertEquals("A1234BC", result.nomisNumber)
    assertNull(result.deadlineDate)
    assertNull(result.status)
    assertNull(result.calculationRule)
    assertNull(result.systemUpdatedBy)
    assertNull(result.systemUpdatedAt)
    assertNull(result.inductionPerformedBy)
    assertNull(result.inductionPerformedAt)
  }

  @Test
  fun `should handle null fields in JSON`() {
    // Given
    val json = """
        {
            "prisonNumber": "A1234BC",
            "deadlineDate": "2023-09-01",
            "scheduleStatus": "SCHEDULED",
            "scheduleCalculationRule": "NEW_PRISON_ADMISSION",
            "updatedByDisplayName": "John Smith",
            "updatedAt": "2023-06-19T09:39:44Z",
            "inductionPerformedBy": null,
            "inductionPerformedAt": null
        }
        """

    // When
    val result = objectMapper.readValue(json, InductionSchedule::class.java)

    // Then
    assertEquals("A1234BC", result.nomisNumber)
    assertEquals(LocalDate.of(2023, 9, 1), result.deadlineDate)
    assertEquals("SCHEDULED", result.status)
    assertEquals("NEW_PRISON_ADMISSION", result.calculationRule)
    assertEquals("John Smith", result.systemUpdatedBy)
    assertEquals(Instant.parse("2023-06-19T09:39:44Z"), result.systemUpdatedAt)
    assertNull(result.inductionPerformedAt)
    assertNull(result.inductionPerformedBy)
  }
}
