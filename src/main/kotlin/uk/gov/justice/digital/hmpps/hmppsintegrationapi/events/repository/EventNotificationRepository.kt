package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import java.time.LocalDateTime

@Repository
interface EventNotificationRepository : JpaRepository<EventNotification, Long> {
  @Modifying
  @Query(
    """
    delete from EventNotification a
    where a.lastModifiedDateTime <= :dateTime and a.status = "PROCESSED"
  """,
  )
  fun deleteEvents(
    @Param("dateTime") dateTime: LocalDateTime,
  )
}
