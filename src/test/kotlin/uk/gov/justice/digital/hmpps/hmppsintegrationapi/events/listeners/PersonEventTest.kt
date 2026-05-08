package uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.listeners

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PRISONER_OFFENDER_SEARCH_PRISONER_CREATED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_CASE_ENGAGEMENT_CREATED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_CASE_PRISON_IDENTIFIER_ADDED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_AMENDED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_CREATED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_DELETED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_MOVED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_TERMINATED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.PROBATION_SENTENCE_UNTERMINATED_MESSAGE
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.DomainEvents.generateDomainEvent
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DomainEventName

class PersonEventTest : DomainEventsListenerTestCase() {
  private val crn = DomainEvents.crn

  @BeforeEach
  internal fun setupPersonTest() {
    assumeIdentities(hmppsId = crn)
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.ProbabtionCase.Engagement.CREATED,
      DomainEventName.ProbabtionCase.PrisonIdentifier.ADDED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED,
      DomainEventName.ProbabtionCase.Sentence.CREATED,
      DomainEventName.ProbabtionCase.Sentence.AMENDED,
      DomainEventName.ProbabtionCase.Sentence.TERMINATED,
      DomainEventName.ProbabtionCase.Sentence.UNTERMINATED,
      DomainEventName.ProbabtionCase.Sentence.DELETED,
      DomainEventName.ProbabtionCase.Sentence.MOVED,
    ],
  )
  fun `process person events`(eventType: String) {
    // Arrange
    val message =
      when (eventType) {
        DomainEventName.ProbabtionCase.Engagement.CREATED -> PROBATION_CASE_ENGAGEMENT_CREATED_MESSAGE
        DomainEventName.ProbabtionCase.PrisonIdentifier.ADDED -> PROBATION_CASE_PRISON_IDENTIFIER_ADDED
        DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED -> PRISONER_OFFENDER_SEARCH_PRISONER_CREATED
        DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED -> PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED
        DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED -> PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED
        DomainEventName.ProbabtionCase.Sentence.CREATED -> PROBATION_SENTENCE_CREATED_MESSAGE
        DomainEventName.ProbabtionCase.Sentence.AMENDED -> PROBATION_SENTENCE_AMENDED_MESSAGE
        DomainEventName.ProbabtionCase.Sentence.TERMINATED -> PROBATION_SENTENCE_TERMINATED_MESSAGE
        DomainEventName.ProbabtionCase.Sentence.UNTERMINATED -> PROBATION_SENTENCE_UNTERMINATED_MESSAGE
        DomainEventName.ProbabtionCase.Sentence.DELETED -> PROBATION_SENTENCE_DELETED_MESSAGE
        DomainEventName.ProbabtionCase.Sentence.MOVED -> PROBATION_SENTENCE_MOVED_MESSAGE
        else -> throw RuntimeException("Unexpected event type: $eventType")
      }

    val payload = generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotification(
      hmppsEventRawMessage = payload,
      expectedNotificationType = IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
    )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.ProbabtionCase.Engagement.CREATED,
      DomainEventName.ProbabtionCase.PrisonIdentifier.ADDED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED,
      DomainEventName.ProbabtionCase.Sentence.CREATED,
    ],
  )
  fun `process new person events`(eventType: String) {
    // Arrange
    val message =
      when (eventType) {
        DomainEventName.ProbabtionCase.Engagement.CREATED -> PROBATION_CASE_ENGAGEMENT_CREATED_MESSAGE
        DomainEventName.ProbabtionCase.PrisonIdentifier.ADDED -> PROBATION_CASE_PRISON_IDENTIFIER_ADDED
        DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED -> PRISONER_OFFENDER_SEARCH_PRISONER_CREATED
        DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED -> PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED
        DomainEventName.ProbabtionCase.Sentence.CREATED -> PROBATION_SENTENCE_CREATED_MESSAGE
        else -> throw RuntimeException("Unexpected event type: $eventType")
      }

    val payload = generateDomainEvent(eventType, message)

    // Act, Assert
    onDomainEventShouldCreateEventNotifications(
      hmppsEventRawMessage = payload,
      IntegrationEventType.PERSON_STATUS_CHANGED.toString(),
      IntegrationEventType.PERSON_CASE_NOTES_CHANGED.toString(),
      IntegrationEventType.PERSON_NAME_CHANGED.toString(),
      IntegrationEventType.PERSON_SENTENCES_CHANGED.toString(),
      IntegrationEventType.PERSON_PROTECTED_CHARACTERISTICS_CHANGED.toString(),
      IntegrationEventType.PERSON_REPORTED_ADJUDICATIONS_CHANGED.toString(),
      IntegrationEventType.PERSON_NUMBER_OF_CHILDREN_CHANGED.toString(),
      IntegrationEventType.PERSON_PHYSICAL_CHARACTERISTICS_CHANGED.toString(),
      IntegrationEventType.PERSON_IMAGES_CHANGED.toString(),
      IntegrationEventType.PERSON_HEALTH_AND_DIET_CHANGED.toString(),
      IntegrationEventType.PERSON_CARE_NEEDS_CHANGED.toString(),
      IntegrationEventType.PERSON_LANGUAGES_CHANGED.toString(),
      IntegrationEventType.KEY_DATES_AND_ADJUSTMENTS_PRISONER_RELEASE.toString(),
      IntegrationEventType.PERSON_ADDRESS_CHANGED.toString(),
      IntegrationEventType.PERSON_CONTACTS_CHANGED.toString(),
      IntegrationEventType.PERSON_IEP_LEVEL_CHANGED.toString(),
      IntegrationEventType.PERSON_VISIT_RESTRICTIONS_CHANGED.toString(),
      IntegrationEventType.PERSON_ALERTS_CHANGED.toString(),
      IntegrationEventType.PERSON_PND_ALERTS_CHANGED.toString(),
      IntegrationEventType.PERSON_RESPONSIBLE_OFFICER_CHANGED.toString(),
    )
  }
}
