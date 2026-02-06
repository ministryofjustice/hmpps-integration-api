package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import java.time.LocalDateTime

@Repository
class EventNotificationRepository(
  private val jdbcTemplate: JdbcTemplate,
) {
  fun deleteEvents(dateTime: LocalDateTime): Int {
    val deleteQuery = "delete from EVENT_NOTIFICATION where LAST_MODIFIED_DATETIME <= ? and STATUS = 'PROCESSED'"
    return jdbcTemplate.update(deleteQuery, dateTime)
  }

  fun saveAll(events: List<EventNotification>): List<EventNotification> {
    events.forEach { event -> save(event) }
    return events
  }

  fun save(eventNotification: EventNotification): Int {
    val insertQuery = """
      insert into EVENT_NOTIFICATION(HMPPS_ID, CLAIM_ID, EVENT_TYPE, PRISON_ID, URL, STATUS, LAST_MODIFIED_DATETIME) values (?,?,?,?,?,?,?)
    """
    return jdbcTemplate.update(insertQuery, eventNotification.hmppsId,  eventNotification.claimId, eventNotification.eventType, eventNotification.prisonId, eventNotification.url, eventNotification.status, eventNotification.lastModifiedDateTime)
  }

  fun deleteAll(): Int {
    val deleteQuery = "delete from EVENT_NOTIFICATION"
    return jdbcTemplate.update(deleteQuery)
  }

  fun count(): Int {
    val countQuery = "select count(*) from EVENT_NOTIFICATION"
    return jdbcTemplate.queryForObject<Int>(countQuery)
  }
}
