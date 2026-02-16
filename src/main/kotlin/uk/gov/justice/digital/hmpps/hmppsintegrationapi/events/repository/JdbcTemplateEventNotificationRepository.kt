package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.queryForObject
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.StuckEvents
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

  override fun getStuckEvents(minusMinutes: LocalDateTime): List<StuckEvents> {
    val getStuckQuery = """
      select count(*) as event_count, status,
      min(last_modified_datetime) as earliest_datetime
      from event_notification
      where status in ('PROCESSING','PENDING')
        and last_modified_datetime < ?
      group by status
      order by earliest_datetime asc
    """
    return jdbcTemplate.query(
      getStuckQuery,
      DataClassRowMapper(StuckEvents::class.java),
      minusMinutes,
    )
  }

  override fun setProcessed(eventId: Any): Int {
    val setProcessedQuery = """
      update EVENT_NOTIFICATION a
      set status = 'PROCESSED'
      where a.event_id = ?
    """

    return jdbcTemplate.update(setProcessedQuery, eventId)
  }

  override fun setPending(eventId: Any): Int {
    val setPendingQuery = """
      update EVENT_NOTIFICATION a
      set status = 'PENDING', claim_id = null
      where a.event_id = ?
    """

    return jdbcTemplate.update(setPendingQuery, eventId)
  }

  override fun setProcessing(
    fiveMinutesAgo: LocalDateTime,
    claimId: String,
  ): Int {
    val setProcessingQuery = """
      update EVENT_NOTIFICATION a
      set claim_id = ?, status = 'PROCESSING'
      where a.event_id in
        (select b.event_id from EVENT_NOTIFICATION b
          where b.last_modified_datetime <= ? and b.status = 'PENDING'
          order by b.last_modified_datetime asc limit ?
        )
    """

    return jdbcTemplate.update(setProcessingQuery, claimId, fiveMinutesAgo, EVENT_NOTIFICATION_BATCH_LIMIT)
  }

  override fun findAllProcessingEvents(claimId: String): List<EventNotification> {
    val findAllProcessingQuery = """
      select * from EVENT_NOTIFICATION a
      where a.status = 'PROCESSING' and a.claim_id = ?
      order by a.last_modified_datetime asc
    """
    return jdbcTemplate.query(
      findAllProcessingQuery,
      DataClassRowMapper(EventNotification::class.java),
      claimId,
    )
  }

  fun saveAll(events: List<EventNotification>): List<EventNotification> {
    events.forEach { event -> save(event) }
    return events
  }

  override fun save(makeEvent: EventNotification): Int {
    val insertQuery = """
      insert into EVENT_NOTIFICATION(
      HMPPS_ID,
      CLAIM_ID,
      EVENT_TYPE,
      PRISON_ID,
      URL,
      STATUS,
      LAST_MODIFIED_DATETIME
      ) values (?,?,?,?,?,?,?)
    """
    return jdbcTemplate.update(insertQuery, makeEvent.hmppsId, makeEvent.claimId, makeEvent.eventType, makeEvent.prisonId, makeEvent.url, makeEvent.status, makeEvent.lastModifiedDatetime)
  }

  override fun deleteAll(): Int {
    // language=Postgres
    val deleteQuery = "delete from EVENT_NOTIFICATION"
    return jdbcTemplate.update(deleteQuery)
  }

  override fun findAll(): List<EventNotification> {
    val getAllQuery = """select * from EVENT_NOTIFICATION""" // Jess - I feel like there should be a limit to this?
    return jdbcTemplate.query(
      getAllQuery,
      DataClassRowMapper(EventNotification::class.java),
    )
  }

  override fun findAllWithLastModifiedDateTimeBefore(dateTimeBefore: LocalDateTime): List<EventNotification> {
    val findAllQuery = "select a from EVENT_NOTIFICATION a where a.last_modified_datetime <= ?"
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
