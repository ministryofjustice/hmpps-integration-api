package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import java.time.LocalDateTime

const val EVENT_NOTIFICATION_BATCH_LIMIT = 1000

@ConditionalOnProperty("feature-flag.enable-delete-processed-events", havingValue = "true")
@Repository
class JdbcTemplateEventNotificationRepository(
  private val jdbcTemplate: JdbcTemplate,
) : EventNotificationRepository {
  override fun deleteEvents(dateTime: LocalDateTime): Int {
    // language=PostgresSql
    val deleteQuery = "delete from EVENT_NOTIFICATION where LAST_MODIFIED_DATETIME <= ? and STATUS = 'PROCESSED'"
    return jdbcTemplate.update(deleteQuery, dateTime)
  }

  override fun getStuckEvents(minusMinutes: LocalDateTime): List<EventNotification> {
    val getStuckQuery = """
      select count(*) as event_count, status,
      min(last_modified_datetime) as earliest_datetime
      from event_notification
      where status in ('PROCESSING','PENDING')
        and last_modified_datetime < ?
      group by status
      order by earliest_datetime asc
    """
    return jdbcTemplate.queryForObject<List<EventNotification>>(getStuckQuery, args = arrayOf(minusMinutes))!!
  }

  override fun setProcessed(eventId: Any): Int {
    val setProcessedQuery = """
      update EventNotification a
      set a.status = "PROCESSED"
      where a.eventId = ?
    """

    return jdbcTemplate.update(setProcessedQuery, eventId)
  }

  override fun setPending(eventId: Any): Int {
    val setPendingQuery = """
      update EventNotification a
      set a.status = "PENDING", a.claimId = null
      where a.eventId = ?
    """

    return jdbcTemplate.update(setPendingQuery, eventId)
  }

  override fun setProcessing(
    fiveMinutesAgo: LocalDateTime,
    claimId: String,
  ): Int {
    val setProcessingQuery = """
      update EventNotification a
      set a.claimId = ?, a.status = "PROCESSING"
      where a.eventId in
        (select b.eventId from EventNotification b
          where b.lastModifiedDateTime <= ? and b.status = "PENDING"
          order by b.lastModifiedDateTime asc limit ?
        )
    """

    return jdbcTemplate.update(setProcessingQuery, claimId, fiveMinutesAgo, EVENT_NOTIFICATION_BATCH_LIMIT)
  }

  override fun findAllProcessingEvents(claimId: String): List<EventNotification> {
    val findAllProcessingQuery = """
      select a from EventNotification a
      where a.status = "PROCESSING" and a.claimId = ?
      order by a.lastModifiedDateTime asc
    """
    return jdbcTemplate.queryForObject<List<EventNotification>>(findAllProcessingQuery, args = arrayOf(claimId))!!
  }

  fun saveAll(events: List<EventNotification>): List<EventNotification> {
    events.forEach { event -> save(event) }
    return events
  }

  override fun save(eventNotification: EventNotification): Int {
    // language=Postgres
    val insertQuery = """
      insert into EVENT_NOTIFICATION(
      HMPPS_ID,
      CLAIM_ID,
      EVENT_TYPE,
      URL,
      STATUS,
      LAST_MODIFIED_DATETIME
    """
    return jdbcTemplate.update(insertQuery, eventNotification.hmppsId, eventNotification.claimId, eventNotification.eventType, eventNotification.prisonId, eventNotification.url, eventNotification.status, eventNotification.lastModifiedDateTime)
  }

  override fun deleteAll(): Int {
    // language=Postgres
    val deleteQuery = "delete from EVENT_NOTIFICATION"
    return jdbcTemplate.update(deleteQuery)
  }

  override fun findAll(): List<EventNotification> {
    val getAllQuery = "select * from EVENT_NOTIFICATION" // Jess - I feel like there should be a limit to this?
    return jdbcTemplate.queryForObject<List<EventNotification>>(getAllQuery)
  }

  override fun findAllWithLastModifiedDateTimeBefore(dateTimeBefore: LocalDateTime): List<EventNotification> {
    val findAllQuery = "select a from EventNotification a where a.lastModifiedDateTime <= ?"
    return jdbcTemplate.queryForObject<List<EventNotification>>(findAllQuery, args = arrayOf(dateTimeBefore))!!
  }

  override fun deleteById(id: Int): Int {
    val deleteByIdQuery = "delete from EVENT_NOTIFICATION where id = ?"
    return jdbcTemplate.update(deleteByIdQuery, id)
  }

  fun count(): Int {
    // language=Postgres
    val countQuery = "select count(*) from EVENT_NOTIFICATION"
    return jdbcTemplate.queryForObject<Int>(countQuery)
  }
}
