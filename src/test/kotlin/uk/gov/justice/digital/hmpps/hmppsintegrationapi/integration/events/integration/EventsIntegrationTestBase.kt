package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.integration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.EventNotificationRepository

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
abstract class EventsIntegrationTestBase {
  @MockitoSpyBean
  lateinit var eventNotificationRepository: EventNotificationRepository
}
