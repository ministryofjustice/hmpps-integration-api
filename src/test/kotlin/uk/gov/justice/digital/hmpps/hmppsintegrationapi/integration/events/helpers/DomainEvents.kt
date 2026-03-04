package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.events.helpers

import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.EventType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.SQSMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.events.entities.SQSMessageAttributes

object DomainEvents {
  val occuredAt = "2024-08-09T12:20:40.282+01:00"
  val crn = "X777776"
  val nomsNumber = "A1234BC"

  val PROBATION_CASE_ENGAGEMENT_CREATED_MESSAGE =
    """
    {\"eventType\":\"probation-case.engagement.created\",\"version\":1,\"detailUrl\":\"https://domain-events-and-delius.hmpps.service.justice.gov.uk/probation-case.engagement.created/$crn\",\"occurredAt\":\"$occuredAt\",\"description\":\"A probation case record for a person has been created in Delius\",\"additionalInformation\":{},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"$crn\"}]}}
    """.trimIndent()

  val PROBATION_CASE_REGISTRATION_UPDATED =
    """
    {\"eventType\":\"probation-case.registration.updated\",\"version\":1,\"occurredAt\":\"$occuredAt\",\"description\":\"A registration has been updated on the probation case\",\"additionalInformation\":{\"registrationLevelDescription\":\"\",\"registerTypeDescription\":\"Victim Contact\",\"registrationCategoryCode\":\"\",\"registrationId\":\"1506296709\",\"registerTypeCode\":\"INVI\",\"updatedDateAndTime\":\"Fri Aug 09 12:24:08 BST 2024\",\"registrationCategoryDescription\":\"\",\"registrationLevelCode\":\"\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"$crn\"},{\"type\":\"NOMS\",\"value\":\"$nomsNumber\"}]}}
    """.trimIndent()

  val PROBATION_CASE_PRISON_IDENTIFIER_ADDED =
    """
    {\"eventType\":\"probation-case.prison-identifier.added\",\"version\":1,\"occurredAt\":\"$occuredAt\",\"description\":\"A probation case has been matched with a booking in the prison system. The prisoner and booking identifiers have been added to the probation case.\",\"additionalInformation\":{\"bookingNumber\":\"81702E\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"$crn\"},{\"type\":\"NOMS\",\"value\":\"$nomsNumber\"}]}}
    """.trimIndent()

  val PRISONER_OFFENDER_SEARCH_PRISONER_CREATED =
    """
    {\"additionalInformation\":{\"nomsNumber\":\"$nomsNumber\"},\"occurredAt\":\"$occuredAt\",\"eventType\":\"prisoner-offender-search.prisoner.created\",\"version\":1,\"description\":\"A prisoner record has been created\",\"detailUrl\":\"https://prisoner-search.prison.service.justice.gov.uk/prisoner/$nomsNumber\"}
    """.trimIndent()

  val PRISONER_OFFENDER_SEARCH_PRISONER_UPDATED =
    """
    {\"additionalInformation\":{\"nomsNumber\":\"$nomsNumber\",\"categoriesChanged\":[\"IDENTIFIERS\"]},\"occurredAt\":\"$occuredAt\",\"eventType\":\"prisoner-offender-search.prisoner.updated\",\"version\":1,\"description\":\"A prisoner record has been updated\",\"detailUrl\":\"https://prisoner-search.prison.service.justice.gov.uk/prisoner/$nomsNumber\"}
    """.trimIndent()

  val PRISONER_OFFENDER_SEARCH_PRISONER_RECEIVED =
    """
    {\"additionalInformation\":{\"nomsNumber\":\"$nomsNumber\",\"categoriesChanged\":[\"IDENTIFIERS\"]},\"occurredAt\":\"$occuredAt\",\"eventType\":\"prisoner-offender-search.prisoner.received\",\"version\":1,\"description\":\"A prisoner record has been updated\",\"detailUrl\":\"https://prisoner-search.prison.service.justice.gov.uk/prisoner/$nomsNumber\"}
    """.trimIndent()

  val ASSESSMENT_SUMMARY_PRODUCED =
    """
    {\"eventType\":\"assessment.summary.produced\",\"version\": 1,\"description\":\"Assessment Summary has been produced\",\"detailUrl\":\"https://oasys.service.justice.gov.uk/eor/oasys/ass/asssumm/R007565/ALLOW/2513077240/COMPLETE\",\"occurredAt\": \"2024-08-14T12:33:34+01:00\",\"personReference\":{\"identifiers\":[{\"type\": \"CRN\", \"value\": \"$crn\"}]}}
    """.trimIndent()

  private val sqsNotificationGeneratingHelper = SqsNotificationGeneratingHelper()

  fun generateDomainEvent(
    eventType: String,
    message: String,
  ) = """
    {
      "Type" : "Notification",
      "MessageId" : "d4419bdd-2079-598c-b608-c4f2ddb1bcd1",
      "TopicArn" : "arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-97e6567cf80881a8a52290ff2c269b08",
      "Message" : "$message",
      "Timestamp" : "2024-08-09T11:20:40.320Z",
      "SignatureVersion" : "1",
      "Signature" : "IMtmzxSgFYKD4fljhMOGSLVPyt0eCduKLN9Y8j9Zr3dbWHgjL9lM4qaMbLo/XPOdz8Cya2N50KGkFf4pAmp8yGAGM56gkJHQFCcIbdHGkW9w86woxjvHb0kh13BAiv7JWwrAvTIgJgPqtph6RCQY385eqGk4jU7JmPvtU+YeZoSv657Qa4LP6DPNjvdmnOYfrXnt+BVyzpVHBlWLnBi9dv+WMnRBxZ36IhppjTQw+hAnlU1yg98r93GRH43d2PLiINlIkyMP7TXH7rYX1RwPCceC9VAeXNJdzCLTteUDCI4trwKloZLYfqpZXgWRzhyB/ZaBJz/wmjA7iKBvtbIdUA==",
      "SigningCertURL" : "https://sns.eu-west-2.amazonaws.com/SimpleNotificationService-60eadc530605d63b8e62a523676ef735.pem",
      "UnsubscribeURL" : "https://sns.eu-west-2.amazonaws.com/?Action=Unsubscribe&SubscriptionArn=arn:aws:sns:eu-west-2:754256621582:cloud-platform-Digital-Prison-Services-97e6567cf80881a8a52290ff2c269b08:340b799a-084f-4027-a214-510087556d97",
      "MessageAttributes" : {
        "traceparent" : {"Type":"String","Value":"00-e46c152a1097400c9c5e8f9b53b26ca5-e1a16aff9e932bba-01"},
        "eventType" : {"Type":"String","Value":"$eventType"},
        "id" : {"Type":"String","Value":"51c928a9-4d16-5e97-1674-02ff2a616177"},
        "timestamp" : {"Type":"Number.java.lang.Long","Value":"1723202440316"}
      }
    }
    """.trimIndent()

  fun generateHmppsDomainEvent(
    eventType: String,
    message: String,
  ) = SQSMessage(
    type = "Notification",
    message = message,
    messageId = "d4419bdd-2079-598c-b608-c4f2ddb1bcd1",
    messageAttributes = SQSMessageAttributes(EventType(eventType)),
  ).let { sqsNotificationGeneratingHelper.extractDomainEventFrom(it) }
}
