package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldContainOnly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.tuple
import org.awaitility.Awaitility
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilAsserted
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventNotification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.EDUCATION_ASSESSMENTS_PRISONER_CHANGED_CATEGORIES
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.IntegrationEventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.PrisonerChangedCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.ReceptionReasons
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.enums.ReleaseReasons
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.helpers.SqsNotificationGeneratingHelper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.models.DomainEventName
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.repository.JdbcTemplateEventNotificationRepository
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.Duration

class DomainEventsListenerIntegrationTest : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  protected val domainEventsQueueConfig by lazy { hmppsQueueService.findByQueueId("hmppsdomainqueue") ?: throw MissingQueueException("HmppsQueue hmppsDomainQueue not found") }
  protected val domainEventsQueueSqsUrl by lazy { domainEventsQueueConfig.queueUrl }
  protected val domainEventsQueueSqsClient by lazy { domainEventsQueueConfig.sqsClient }

  protected fun sendDomainSqsMessage(rawMessage: String) {
    domainEventsQueueSqsClient.sendMessage(
      SendMessageRequest
        .builder()
        .queueUrl(domainEventsQueueSqsUrl)
        .messageBody(rawMessage)
        .build(),
    )
  }

  @Autowired
  lateinit var repo: JdbcTemplateEventNotificationRepository
  val prisonId = "MDI"

  val awaitTimeOut = Duration.ofSeconds(5)
  val awaitPollDelay = Duration.ofMillis(200)
  val defaultAwaitTimeOutNoEventSaved = Duration.ofSeconds(2)

  @BeforeEach
  fun setup() {
    repo.deleteAll()
    Awaitility.setDefaultTimeout(awaitTimeOut)
    Awaitility.setDefaultPollDelay(awaitPollDelay)
  }

  @AfterEach
  fun cleanup() {
    Awaitility.reset()
  }

  @Test
  fun `will process and save a valid domain event SQS message`() {
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawGenericEvent()
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
  }

//  @Test
//  fun `will not process a malformed domain event SQS Message and log to dead letter queue`() {
//    sendDomainSqsMessage("BAD JSON")
//
//    Awaitility.await().until { getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue() == 1 }
//    val deadLetterQueueMessage = geMessagesCurrentlyOnDomainEventsDeadLetterQueue()
//    val message = deadLetterQueueMessage.messages().first()
//    message.body().shouldBe("BAD JSON")
//    val savedEvent = repo.findAll().firstOrNull()
//    savedEvent.shouldBeNull()
//  }

//  @Test
//  fun `will not process and save a domain event message with an unknown type`() {
//    val rawMessage = SqsNotificationGeneratingHelper().generateRawGenericEvent(eventTypeValue = "some.other-event")
//
//    sendDomainSqsMessage(rawMessage)
//
//    Awaitility.await().until { getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue() == 0 }
//    val savedEvent = repo.findAll().firstOrNull()
//    savedEvent.shouldBeNull()
//  }

  @Test
  fun `will not process and save a domain event message with an unknown register type code`() {
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawHmppsDomainEvent(registerTypeCode = "OtherType")

    sendDomainSqsMessage(rawMessage)

    val savedEvent = repo.findAll().firstOrNull()

    savedEvent.shouldBeNull()
  }

//  @Test
//  fun `will not process and save a domain event message with no crn type and log to dead letter queue`() {
//    val rawMessage = SqsNotificationGeneratingHelper().generateRawHmppsDomainEvent(identifiers = "[]")
//
//    sendDomainSqsMessage(rawMessage)
//
//    Awaitility.await().until { getNumberOfMessagesCurrentlyOndomainEventsDeadLetterQueue() == 1 }
//    val deadLetterQueueMessage = geMessagesCurrentlyOnDomainEventsDeadLetterQueue()
//    val message = deadLetterQueueMessage.messages().first()
//    val payload = message.body()
//    payload.shouldBe(rawMessage)
//    val savedEvent = repo.findAll().firstOrNull()
//    savedEvent.shouldBeNull()
//  }

  // Specific event tests

  @Test
  fun `will process and save a prisoner released event SQS message`() {
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generatePrisonerReleasedEvent()
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
    savedEvent.eventType.shouldBe(IntegrationEventType.KEY_DATES_AND_ADJUSTMENTS_PRISONER_RELEASE.name)
    savedEvent.hmppsId.shouldBe(crn)
    savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/sentences/latest-key-dates-and-adjustments")
  }

  @Test
  fun `will process and save a prisoner merge event SQS message`() {
    val rawMessage = SqsNotificationGeneratingHelper().generateRawHmppsMergeDomainEvent()

    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until {
      repo.findAll().isNotEmpty()
    }
    val savedEvents: List<EventNotification> = repo.findByHmppsIdIsIn(listOf("A3646EA", "A3646EB"))
    savedEvents.shouldNotBeEmpty().shouldHaveSize(2)

    Assertions
      .assertThat(savedEvents)
      .extracting(
        EventNotification::eventType,
        EventNotification::hmppsId,
        EventNotification::url,
      ).containsExactlyInAnyOrder(
        tuple(IntegrationEventType.PERSON_STATUS_CHANGED.name, "A3646EA", "https://localhost:8443/v1/persons/A3646EA"),
        tuple(IntegrationEventType.PRISONER_MERGED.name, "A3646EB", "https://localhost:8443/v1/persons/A3646EB"),
      )
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_ADDED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_APPROVED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_UNAPPROVED,
      DomainEventName.PrisonOffenderEvents.Prisoner.CONTACT_REMOVED,
    ],
  )
  fun `will process and save a person contacts event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": 1,
      "description": "A contact has been added to a prisoner",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
    savedEvent.eventType.shouldBe(IntegrationEventType.PERSON_CONTACTS_CHANGED.name)
    savedEvent.hmppsId.shouldBe(crn)
    savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/contacts")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Incentives.IEPReview.INSERTED,
      DomainEventName.Incentives.IEPReview.UPDATED,
      DomainEventName.Incentives.IEPReview.DELETED,
    ],
  )
  fun `will process and save a person iep event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "An IEP review has been changed",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "nomsNumber": "$nomsId"
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
    savedEvent.eventType.shouldBe(IntegrationEventType.PERSON_IEP_LEVEL_CHANGED.name)
    savedEvent.hmppsId.shouldBe(crn)
    savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/iep-level")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.PersonRestriction.UPSERTED,
      DomainEventName.PrisonOffenderEvents.Prisoner.PersonRestriction.DELETED,
    ],
  )
  fun `will process and save a visitor restriction event SQS message`(eventType: String) {
    val contactId = "7551236"
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "This event is raised when a global visitor restriction is created or updated.",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "contactPersonId": "$contactId"
      },
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
    savedEvent.eventType.shouldBe(IntegrationEventType.PERSON_VISITOR_RESTRICTIONS_CHANGED.name)
    savedEvent.hmppsId.shouldBe(crn)
    savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/visitor/$contactId/restrictions")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.Restriction.CHANGED,
    ],
  )
  fun `will process and save a visit restriction event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "This event is raised when a prisoner visits restriction is created/updated/deleted",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvent = repo.findAll().firstOrNull()
    savedEvent.shouldNotBeNull()
    savedEvent.eventType.shouldBe(IntegrationEventType.PERSON_VISIT_RESTRICTIONS_CHANGED.name)
    savedEvent.hmppsId.shouldBe(crn)
    savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/visit-restrictions")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonVisit.BOOKED,
      DomainEventName.PrisonVisit.CHANGED,
      DomainEventName.PrisonVisit.CANCELLED,
    ],
  )
  fun `will process and save a visit changed event SQS message`(eventType: String) {
    val visitReference = "nx-ce-vq-ry"
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "Prison visit changed",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "prisonerId": "$nomsId",
      "additionalInformation": {
        "reference": "$visitReference"
      }
    }
    """
    val rawMessage = SqsNotificationGeneratingHelper().generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(3)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_FUTURE_VISITS_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn/visit/future")
    savedEvents[1].eventType.shouldBe(IntegrationEventType.PRISON_VISITS_CHANGED.name)
    savedEvents[1].hmppsId.shouldBe(crn)
    savedEvents[1].url.shouldBe("https://localhost:8443/v1/prison/$prisonId/visit/search")
    savedEvents[2].eventType.shouldBe(IntegrationEventType.VISIT_CHANGED.name)
    savedEvents[2].hmppsId.shouldBe(crn)
    savedEvents[2].url.shouldBe("https://localhost:8443/v1/visit/$visitReference")
  }

  @Nested
  @DisplayName("Given a prisoner updated domain event")
  inner class GivenPrisonerUpdatedDomainEvent {
    private val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED

    private fun generateMessage(vararg categoriesChanged: String) =
      """
      {
        "eventType": "$eventType",
        "version": "1.0",
        "description": "This is when a prisoner index record has been updated.",
        "occurredAt": "2024-08-14T12:33:34+01:00",
        "additionalInformation": {
          "categoriesChanged": [${categoriesChanged.joinToString { "\"$it\"" }}]
        },
        "personReference": {
          "identifiers": [
            {
              "type": "NOMS",
              "value": "$nomsId"
             }
          ]
        }
      }
      """

    private fun generateMessage(categoriesChanged: PrisonerChangedCategory) = generateMessage(categoriesChanged.name)

    @Test
    fun `will process and save a prisoner personal details changed event SQS message`() {
      val message = generateMessage(PrisonerChangedCategory.PERSONAL_DETAILS.name)
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvents = repo.findAll()
      savedEvents.size.shouldBe(4)
      savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_STATUS_CHANGED.name)
      savedEvents[0].hmppsId.shouldBe(crn)
      savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn")
      savedEvents[1].eventType.shouldBe(IntegrationEventType.PERSON_NAME_CHANGED.name)
      savedEvents[1].hmppsId.shouldBe(crn)
      savedEvents[1].url.shouldBe("https://localhost:8443/v1/persons/$crn/name")
      savedEvents[2].eventType.shouldBe(IntegrationEventType.PRISONERS_CHANGED.name)
      savedEvents[2].hmppsId.shouldBe(crn)
      savedEvents[2].url.shouldBe("https://localhost:8443/v1/prison/prisoners")
      savedEvents[3].eventType.shouldBe(IntegrationEventType.PRISONER_CHANGED.name)
      savedEvents[3].hmppsId.shouldBe(crn)
      savedEvents[3].url.shouldBe("https://localhost:8443/v1/prison/prisoners/$crn")
    }

    @Test
    fun `will process and save a prisoner sentences changed event SQS message`() {
      val message = generateMessage(PrisonerChangedCategory.SENTENCE)
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvents = repo.findAll()
      savedEvents.size.shouldBe(5)
      savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_STATUS_CHANGED.name)
      savedEvents[0].hmppsId.shouldBe(crn)
      savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn")
      savedEvents[1].eventType.shouldBe(IntegrationEventType.PERSON_SENTENCES_CHANGED.name)
      savedEvents[1].hmppsId.shouldBe(crn)
      savedEvents[1].url.shouldBe("https://localhost:8443/v1/persons/$crn/sentences")
      savedEvents[2].eventType.shouldBe(IntegrationEventType.PRISONERS_CHANGED.name)
      savedEvents[2].hmppsId.shouldBe(crn)
      savedEvents[2].url.shouldBe("https://localhost:8443/v1/prison/prisoners")
      savedEvents[3].eventType.shouldBe(IntegrationEventType.PRISONER_CHANGED.name)
      savedEvents[3].hmppsId.shouldBe(crn)
      savedEvents[3].url.shouldBe("https://localhost:8443/v1/prison/prisoners/$crn")
      savedEvents[4].eventType.shouldBe(IntegrationEventType.PERSON_EDUCATION_ASSESSMENTS_CHANGED.name)
      savedEvents[4].hmppsId.shouldBe(crn)
      savedEvents[4].url.shouldBe("https://localhost:8443/v1/persons/$crn/education/assessments")
    }

    @Test
    fun `will process and save a prisoner physical details changed event SQS message`() {
      val message = generateMessage(PrisonerChangedCategory.PHYSICAL_DETAILS)
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvents = repo.findAll()
      savedEvents.size.shouldBe(4)
      savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_STATUS_CHANGED.name)
      savedEvents[0].hmppsId.shouldBe(crn)
      savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn")
      savedEvents[1].eventType.shouldBe(IntegrationEventType.PERSON_PHYSICAL_CHARACTERISTICS_CHANGED.name)
      savedEvents[1].hmppsId.shouldBe(crn)
      savedEvents[1].url.shouldBe("https://localhost:8443/v1/persons/$crn/physical-characteristics")
      savedEvents[2].eventType.shouldBe(IntegrationEventType.PRISONERS_CHANGED.name)
      savedEvents[2].hmppsId.shouldBe(crn)
      savedEvents[2].url.shouldBe("https://localhost:8443/v1/prison/prisoners")
      savedEvents[3].eventType.shouldBe(IntegrationEventType.PRISONER_CHANGED.name)
      savedEvents[3].hmppsId.shouldBe(crn)
      savedEvents[3].url.shouldBe("https://localhost:8443/v1/prison/prisoners/$crn")
    }

    @Test
    fun `will process and save a prisoner location changed event SQS message`() {
      val message = generateMessage(PrisonerChangedCategory.LOCATION)
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvents = repo.findAll()
      savedEvents.size.shouldBe(5)
      savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_STATUS_CHANGED.name)
      savedEvents[0].hmppsId.shouldBe(crn)
      savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn")
      savedEvents[1].eventType.shouldBe(IntegrationEventType.PERSON_CELL_LOCATION_CHANGED.name)
      savedEvents[1].hmppsId.shouldBe(crn)
      savedEvents[1].url.shouldBe("https://localhost:8443/v1/persons/$crn/cell-location")
      savedEvents[2].eventType.shouldBe(IntegrationEventType.PRISONERS_CHANGED.name)
      savedEvents[2].hmppsId.shouldBe(crn)
      savedEvents[2].url.shouldBe("https://localhost:8443/v1/prison/prisoners")
      savedEvents[3].eventType.shouldBe(IntegrationEventType.PRISONER_CHANGED.name)
      savedEvents[3].hmppsId.shouldBe(crn)
      savedEvents[3].url.shouldBe("https://localhost:8443/v1/prison/prisoners/$crn")
      savedEvents[4].eventType.shouldBe(IntegrationEventType.PERSON_EDUCATION_ASSESSMENTS_CHANGED.name)
      savedEvents[4].hmppsId.shouldBe(crn)
      savedEvents[4].url.shouldBe("https://localhost:8443/v1/persons/$crn/education/assessments")
    }

    @Nested
    @DisplayName("and Education Assessments Integration Event is expected or not.")
    inner class AndEducationAssessmentIntegrationEventIsExpectedOrNot {
      private val intEventType = IntegrationEventType.PERSON_EDUCATION_ASSESSMENTS_CHANGED
      private val url = "https://localhost:8443/v1/persons/$crn/education/assessments"

      @ParameterizedTest
      @MethodSource("${CLASS_QUALIFIED_NAME}#educationAssessmentCategoryProvider")
      fun `will process and save a prisoner education assessments change event SQS message for expected categories`(changedCategory: String) {
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message = generateMessage(changedCategory))
          .also { sendDomainSqsMessage(it) }

        awaitAndAssertEventIsSaved(intEventType, url)
      }

      @Test
      fun `will not process and save a prisoner education assessments change event SQS message for filtered categories`() {
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(
            eventType,
            message = generateMessage(PrisonerChangedCategory.PHYSICAL_DETAILS),
          ).also { sendDomainSqsMessage(it) }

        awaitAndAssertEventNotSaved(intEventType, url)
      }
    }
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonerOffenderSearch.Prisoner.CREATED,
      DomainEventName.PrisonerOffenderSearch.Prisoner.RECEIVED,
    ],
  )
  fun `will process and save a prisoner created + received event SQS messages`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "This is when a prisoner index record has been updated.",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "categoriesChanged": []
      },
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(24)
    val eventTypes = savedEvents.map { it.eventType }
    val hmppsIds = savedEvents.map { it.hmppsId }
    val urls = savedEvents.map { it.url }

    eventTypes.shouldContainExactlyInAnyOrder(
      IntegrationEventType.PERSON_STATUS_CHANGED.name,
      IntegrationEventType.PERSON_CASE_NOTES_CHANGED.name,
      IntegrationEventType.PERSON_NAME_CHANGED.name,
      IntegrationEventType.PERSON_CELL_LOCATION_CHANGED.name,
      IntegrationEventType.PERSON_SENTENCES_CHANGED.name,
      IntegrationEventType.PERSON_PROTECTED_CHARACTERISTICS_CHANGED.name,
      IntegrationEventType.PERSON_REPORTED_ADJUDICATIONS_CHANGED.name,
      IntegrationEventType.PERSON_NUMBER_OF_CHILDREN_CHANGED.name,
      IntegrationEventType.PERSON_PHYSICAL_CHARACTERISTICS_CHANGED.name,
      IntegrationEventType.PERSON_IMAGES_CHANGED.name,
      IntegrationEventType.PERSON_HEALTH_AND_DIET_CHANGED.name,
      IntegrationEventType.PERSON_CARE_NEEDS_CHANGED.name,
      IntegrationEventType.PERSON_LANGUAGES_CHANGED.name,
      IntegrationEventType.PRISONERS_CHANGED.name,
      IntegrationEventType.PRISONER_CHANGED.name,
      IntegrationEventType.PRISONER_NON_ASSOCIATIONS_CHANGED.name,
      IntegrationEventType.KEY_DATES_AND_ADJUSTMENTS_PRISONER_RELEASE.name,
      IntegrationEventType.PERSON_ADDRESS_CHANGED.name,
      IntegrationEventType.PERSON_CONTACTS_CHANGED.name,
      IntegrationEventType.PERSON_IEP_LEVEL_CHANGED.name,
      IntegrationEventType.PERSON_VISIT_RESTRICTIONS_CHANGED.name,
      IntegrationEventType.PERSON_ALERTS_CHANGED.name,
      IntegrationEventType.PERSON_PND_ALERTS_CHANGED.name,
      IntegrationEventType.PERSON_RESPONSIBLE_OFFICER_CHANGED.name,
    )
    hmppsIds.shouldContainOnly(crn)
    urls.shouldContainExactlyInAnyOrder(
      "https://localhost:8443/v1/persons/$crn",
      "https://localhost:8443/v1/persons/$crn/case-notes",
      "https://localhost:8443/v1/persons/$crn/name",
      "https://localhost:8443/v1/persons/$crn/cell-location",
      "https://localhost:8443/v1/persons/$crn/sentences",
      "https://localhost:8443/v1/persons/$crn/protected-characteristics",
      "https://localhost:8443/v1/persons/$crn/reported-adjudications",
      "https://localhost:8443/v1/persons/$crn/number-of-children",
      "https://localhost:8443/v1/persons/$crn/physical-characteristics",
      "https://localhost:8443/v1/persons/$crn/images",
      "https://localhost:8443/v1/persons/$crn/health-and-diet",
      "https://localhost:8443/v1/persons/$crn/care-needs",
      "https://localhost:8443/v1/persons/$crn/languages",
      "https://localhost:8443/v1/prison/prisoners",
      "https://localhost:8443/v1/prison/prisoners/$crn",
      "https://localhost:8443/v1/prison/$prisonId/prisoners/$crn/non-associations",
      "https://localhost:8443/v1/persons/$crn/sentences/latest-key-dates-and-adjustments",
      "https://localhost:8443/v1/persons/$crn/addresses",
      "https://localhost:8443/v1/persons/$crn/contacts",
      "https://localhost:8443/v1/persons/$crn/iep-level",
      "https://localhost:8443/v1/persons/$crn/visit-restrictions",
      "https://localhost:8443/v1/persons/$crn/alerts",
      "https://localhost:8443/v1/pnd/persons/$crn/alerts",
      "https://localhost:8443/v1/persons/$crn/person-responsible-officer",
    )
  }

  @Test
  fun `will process and save a prisoner updated event SQS message`() {
    val eventType = DomainEventName.PrisonerOffenderSearch.Prisoner.UPDATED
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "This is when a prisoner index record has been updated.",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "categoriesChanged": []
      },
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(3)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_STATUS_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn")
    savedEvents[1].eventType.shouldBe(IntegrationEventType.PRISONERS_CHANGED.name)
    savedEvents[1].hmppsId.shouldBe(crn)
    savedEvents[1].url.shouldBe("https://localhost:8443/v1/prison/prisoners")
    savedEvents[2].eventType.shouldBe(IntegrationEventType.PRISONER_CHANGED.name)
    savedEvents[2].hmppsId.shouldBe(crn)
    savedEvents[2].url.shouldBe("https://localhost:8443/v1/prison/prisoners/$crn")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Person.CaseNote.CREATED,
      DomainEventName.Person.CaseNote.UPDATED,
      DomainEventName.Person.CaseNote.DELETED,
    ],
  )
  fun `will process and save a case note changed event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "A case note has been created for a person",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(1)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_CASE_NOTES_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn/case-notes")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.Adjudication.Hearing.CREATED,
      DomainEventName.Adjudication.Hearing.DELETED,
      DomainEventName.Adjudication.Hearing.COMPLETED,
      DomainEventName.Adjudication.Punishments.CREATED,
      DomainEventName.Adjudication.Report.CREATED,
    ],
  )
  fun `will process and save a adjudication changed event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "An adjudication has been created:  MDI-000169",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "prisonerNumber": "$nomsId"
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(1)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.PERSON_REPORTED_ADJUDICATIONS_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn/reported-adjudications")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.PrisonOffenderEvents.Prisoner.NonAssociationDetail.CHANGED,
    ],
  )
  fun `will process and save a non-association event SQS message`(eventType: String) {
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "A prisoner non-association detail record has changed",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(1)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.PRISONER_NON_ASSOCIATIONS_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/prison/$prisonId/prisoners/$crn/non-associations")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.LocationsInsidePrison.Location.CREATED,
      DomainEventName.LocationsInsidePrison.Location.AMENDED,
      DomainEventName.LocationsInsidePrison.Location.DELETED,
      DomainEventName.LocationsInsidePrison.Location.DEACTIVATED,
      DomainEventName.LocationsInsidePrison.Location.REACTIVATED,
    ],
  )
  fun `will process and save a location event SQS message`(eventType: String) {
    val locationKey = "$prisonId-001-01"
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "Locations – a location inside prison has been amended",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "key": "$locationKey"
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    val eventTypes = savedEvents.map { it.eventType }
    val urls = savedEvents.map { it.url }

    eventTypes.shouldContain(IntegrationEventType.PRISON_LOCATION_CHANGED.name)
    eventTypes.shouldContain(IntegrationEventType.PRISON_RESIDENTIAL_HIERARCHY_CHANGED.name)
    eventTypes.shouldContain(IntegrationEventType.PRISON_RESIDENTIAL_DETAILS_CHANGED.name)

    urls.shouldContain("https://localhost:8443/v1/prison/$prisonId/location/$locationKey")
    urls.shouldContain("https://localhost:8443/v1/prison/$prisonId/residential-hierarchy")
    urls.shouldContain("https://localhost:8443/v1/prison/$prisonId/residential-details")
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      DomainEventName.LocationsInsidePrison.Location.CREATED,
      DomainEventName.LocationsInsidePrison.Location.DELETED,
      DomainEventName.LocationsInsidePrison.Location.DEACTIVATED,
      DomainEventName.LocationsInsidePrison.Location.REACTIVATED,
      DomainEventName.LocationsInsidePrison.SignedOpCapacity.AMENDED,
    ],
  )
  fun `will process and save a prison capacity event SQS message`(eventType: String) {
    val locationKey = "$prisonId-001-01"
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "Locations – a location inside prison has been amended",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "key": "$locationKey"
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    val eventTypes = savedEvents.map { it.eventType }
    val urls = savedEvents.map { it.url }

    eventTypes.shouldContain(IntegrationEventType.PRISON_CAPACITY_CHANGED.name)
    urls.shouldContain("https://localhost:8443/v1/prison/$prisonId/capacity")
  }

  @Test
  fun `will process and save a san create schedule event SQS message`() {
    val eventType = DomainEventName.SAN.PlanCreationSchedule.UPDATED
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "A Support for additional needs plan creation schedule created or amended",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(1)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.SAN_PLAN_CREATION_SCHEDULE_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn/education/san/plan-creation-schedule")
  }

  @Test
  fun `will process and save a san review event SQS message`() {
    val eventType = DomainEventName.SAN.ReviewSchedule.UPDATED
    val message = """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "A Support for additional needs review schedule was created or amended",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """
    val rawMessage =
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, message)
    sendDomainSqsMessage(rawMessage)

    Awaitility.await().until { repo.findAll().isNotEmpty() }
    val savedEvents = repo.findAll()
    savedEvents.size.shouldBe(1)
    savedEvents[0].eventType.shouldBe(IntegrationEventType.SAN_REVIEW_SCHEDULE_CHANGED.name)
    savedEvents[0].hmppsId.shouldBe(crn)
    savedEvents[0].url.shouldBe("https://localhost:8443/v1/persons/$crn/education/san/review-schedule")
  }

  @Nested
  @DisplayName("Given a prisoner received domain event")
  inner class GivenPrisonerReceivedDomainEvent {
    private val eventType = DomainEventName.PrisonOffenderEvents.Prisoner.RECEIVED
    private val intEventType = IntegrationEventType.PRISONER_BASE_LOCATION_CHANGED

    private fun generateMessage(reason: String) =
      """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "This is when a A prisoner has been received into prison.",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "currentLocation": "IN_PRISON",
        "currentPrisonStatus": "UNDER_PRISON_CARE",
        "details": "ACTIVE IN:ADM-N",
        "nomisMovementReasonCode": "N",
        "nomsId": "$nomsId",
        "prisonId": "$prisonId",
        "reason": "$reason"
      },
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """

    @ParameterizedTest
    @ValueSource(
      strings = [
        ReceptionReasons.ADMISSION,
        ReceptionReasons.TRANSFERRED,
      ],
    )
    fun `will process and save a received prisoner base location change event SQS message`(reason: String) {
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, generateMessage(reason))
        .also { sendDomainSqsMessage(it) }

      awaitAndAssertEventIsSaved(intEventType, "https://localhost:8443/v1/persons/$crn/prisoner-base-location")
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        ReceptionReasons.TEMPORARY_ABSENCE_RETURN,
        ReceptionReasons.RETURN_FROM_COURT,
      ],
    )
    fun `will not process or save a received prisoner base location change event SQS message for filtered reception reasons`(filteredReason: String) {
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, generateMessage(filteredReason))
        .also { sendDomainSqsMessage(it) }

      awaitAndAssertNoEventSaved()
    }
  }

  @Nested
  @DisplayName("Given a prisoner released domain event")
  inner class GivenPrisonerReleasedDomainEvent {
    private val eventType = DomainEventName.PrisonOffenderEvents.Prisoner.RELEASED
    private val intEventType = IntegrationEventType.PRISONER_BASE_LOCATION_CHANGED

    private fun generateMessage(reason: String) =
      """
    {
      "eventType": "$eventType",
      "version": "1.0",
      "description": "A prisoner has been released from prison",
      "occurredAt": "2024-08-14T12:33:34+01:00",
      "additionalInformation": {
        "currentLocation": "OUTSIDE_PRISON",
        "currentPrisonStatus": "NOT_UNDER_PRISON_CARE",
        "details": "Movement reason code CR",
        "nomisMovementReasonCode": "CR",
        "nomsId": "$nomsId",
        "prisonId": "$prisonId",
        "reason": "$reason"
      },
      "personReference": {
        "identifiers": [
          {
            "type": "NOMS",
            "value": "$nomsId"
           }
        ]
      }
    }
    """

    @Test
    fun `will process and save a released prisoner base location change event SQS message`() {
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, generateMessage(ReleaseReasons.RELEASED))
        .also { sendDomainSqsMessage(it) }

      awaitAndAssertEventIsSaved(intEventType, "https://localhost:8443/v1/persons/$crn/prisoner-base-location")
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        ReleaseReasons.RELEASED_TO_HOSPITAL,
        ReleaseReasons.SENT_TO_COURT,
        ReleaseReasons.TEMPORARY_ABSENCE_RELEASE,
        ReleaseReasons.TRANSFERRED,
      ],
    )
    fun `will not process or save a released prisoner base location change event SQS message for filtered release reasons`(filteredReason: String) {
      SqsNotificationGeneratingHelper()
        .generateRawDomainEvent(eventType, generateMessage(filteredReason))
        .also { sendDomainSqsMessage(it) }

      awaitAndAssertEventNotSaved(IntegrationEventType.PRISONER_BASE_LOCATION_CHANGED)
    }
  }

  @Nested
  @DisplayName("Contact Event domain events")
  inner class GivenContactEventDomainEvent {
    @ParameterizedTest
    @ValueSource(
      strings = [
        DomainEventName.ProbabtionCase.MappaExport.CREATED,
        DomainEventName.ProbabtionCase.MappaInformation.CREATED,
        DomainEventName.ProbabtionCase.AssessmentSummary.CREATED,
        DomainEventName.ProbabtionCase.Cas3Booking.CREATED,
        DomainEventName.ProbabtionCase.SupervisionAppointment.CREATED,
        DomainEventName.ProbabtionCase.Supervision.CREATED,
      ],
    )
    fun `will process the domain event and create a CONTACT_EVENT_CREATED integration event for `(eventType: String) {
      val contactEventId = "1234"
      val message = """
      {
        "eventType": "$eventType",
        "version": 1,
        "description": "A contact event",
        "occurredAt": "2026-01-15T12:33:34+01:00",
        "additionalInformation": {
          "contactId": $contactEventId,
          "mappa": {
            "category": 1
          }
        },
        "personReference": {
          "identifiers": [
            {
              "type": "CRN",
              "value": "$crn"
             }
          ]
        }
      }
      """
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvent = repo.findAll().firstOrNull()
      savedEvent.shouldNotBeNull()
      savedEvent.eventType.shouldBe(IntegrationEventType.CONTACT_EVENT_CREATED.name)
      savedEvent.hmppsId.shouldBe(crn)
      savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/contact-events/$contactEventId")
    }

    @ParameterizedTest
    @ValueSource(
      strings = [
        DomainEventName.ProbabtionCase.MappaInformation.UPDATED,
        DomainEventName.ProbabtionCase.MappaExport.TERMINATED,
        DomainEventName.ProbabtionCase.MappaInformation.DELETED,
      ],
    )
    fun `will process the domain event and create a CONTACT_EVENT_CHANGED integration event for `(eventType: String) {
      val contactEventId = "1234"
      val message = """
      {
        "eventType": "$eventType",
        "version": 1,
        "description": "A contact event",
        "occurredAt": "2026-01-15T12:33:34+01:00",
        "additionalInformation": {
          "contactId": $contactEventId,
          "mappa": {
            "category": 1
          }
        },
        "personReference": {
          "identifiers": [
            {
              "type": "CRN",
              "value": "$crn"
             }
          ]
        }
      }
      """
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvent = repo.findAll().firstOrNull()
      savedEvent.shouldNotBeNull()
      savedEvent.eventType.shouldBe(IntegrationEventType.CONTACT_EVENT_CHANGED.name)
      savedEvent.hmppsId.shouldBe(crn)
      savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/contact-events/$contactEventId")
    }
  }

  @Nested
  @DisplayName("Restriction and exclusion domain events")
  inner class GivenRestrictionAndExclusionDomainEvents {
    @ParameterizedTest
    @ValueSource(
      strings = [
        DomainEventName.ProbabtionCase.Exclusion.UPDATED,
        DomainEventName.ProbabtionCase.Restriction.UPDATED,
      ],
    )
    fun `will process the domain event and create a PERSON_ACCESS_LIMITATIONS_CHANGED integration event for `(eventType: String) {
      val message = """
      {
          "eventType": "$eventType",
          "version": 1,
          "occurredAt": "2026-01-28T16:55:03.801935166Z",
          "description": "An exclusion or restriction has been updated in Delius",
          "additionalInformation": {},
          "personReference": {
              "identifiers": [
                  {
                      "type": "CRN",
                      "value": "$crn"
                  }
              ]
          }
      }
      """ // language=json
      val rawMessage =
        SqsNotificationGeneratingHelper()
          .generateRawDomainEvent(eventType, message)
      sendDomainSqsMessage(rawMessage)

      Awaitility.await().until { repo.findAll().isNotEmpty() }
      val savedEvent = repo.findAll().firstOrNull()
      savedEvent.shouldNotBeNull()
      savedEvent.eventType.shouldBe(IntegrationEventType.PERSON_ACCESS_LIMITATIONS_CHANGED.name)
      savedEvent.hmppsId.shouldBe(crn)
      savedEvent.url.shouldBe("https://localhost:8443/v1/persons/$crn/access-limitations")
    }
  }

  companion object {
    @JvmStatic
    fun educationAssessmentCategoryProvider() = EDUCATION_ASSESSMENTS_PRISONER_CHANGED_CATEGORIES.map { Arguments.of(it) }
  }

  private fun awaitTimeout(timeout: Duration? = defaultAwaitTimeOutNoEventSaved) = await.let { timeout?.let { t -> it.timeout(t) } ?: it }

  /**
   * await until timeout, that no event has been saved
   */
  private fun awaitAndAssertNoEventSaved(timeout: Duration? = null) = awaitTimeout(timeout) untilAsserted { assertThat(repo.findAll(), empty()) }

  /**
   * await until some event(s) are saved, and then check the given integration event type is not saved.
   */
  private fun awaitAndAssertEventNotSaved(
    eventType: IntegrationEventType,
    url: String? = null,
  ) {
    await until { repo.findAll().isNotEmpty() }
    repo.findAll().let { event ->
      event.map { it.eventType }.toSet() shouldNotContain eventType
      url?.let { url ->
        event.map { it.url }.toSet() shouldNotContain url
      }
    }
  }

  /**
   * await until some event(s) are saved, and then check the given integration event type is saved
   */
  private fun awaitAndAssertEventIsSaved(
    eventType: IntegrationEventType,
    url: String,
  ) {
    await until { repo.findAll().isNotEmpty() }
    repo.findAll().let { event ->
      event.map { it.eventType }.toSet() shouldContain eventType.name
      event.map { it.url }.toSet() shouldContain url
    }
  }
}

private const val CLASS_QUALIFIED_NAME = "uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.DomainEventsListenerIntegrationTest"
