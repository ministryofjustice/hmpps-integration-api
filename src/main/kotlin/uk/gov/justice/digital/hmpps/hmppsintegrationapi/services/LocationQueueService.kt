package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.MessageFailedException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.eventTypeMessageAttributes

@Service
class LocationQueueService(
  @Autowired private val hmppsQueueService: HmppsQueueService,
  @Autowired private val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @Autowired private val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @Autowired private val objectMapper: ObjectMapper,
) {
  private val locationQueue by lazy { hmppsQueueService.findByQueueId("locations") as HmppsQueue }
  private val locationQueueSqsClient by lazy { locationQueue.sqsClient }
  private val locationQueueUrl by lazy { locationQueue.queueUrl }

  fun sendDeactivateLocationRequest(
    deactivateLocationRequest: DeactivateLocationRequest,
    prisonId: String,
    key: String,
    who: String,
    filters: ConsumerFilters?,
  ): Response<HmppsMessageResponse?> {
    if (filters?.prisons != null) {
      val consumerPrisonFilterCheck = consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters)
      if (consumerPrisonFilterCheck.errors.isNotEmpty()) {
        return consumerPrisonFilterCheck
      }
    }

    val locationResponse = locationsInsidePrisonGateway.getLocationByKey(key)
    if (locationResponse.errors.isNotEmpty()) {
      return Response(data = null, errors = locationResponse.errors)
    }

    val locationId = locationResponse.data?.id ?: return Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON, type = UpstreamApiError.Type.ENTITY_NOT_FOUND)))
    if (locationResponse.data.locationType != "CELL") {
      return Response(data = null, errors = listOf(UpstreamApiError(causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON, type = UpstreamApiError.Type.BAD_REQUEST, description = "Location type must be a CELL")))
    }

    val hmppsMessage = deactivateLocationRequest.toHmppsMessage(locationId, actionedBy = who)
    writeMessageToQueue(hmppsMessage, "Could not send deactivate location to queue")

    return Response(HmppsMessageResponse(message = "Deactivate location written to queue"))
  }

  private fun writeMessageToQueue(
    hmppsMessage: HmppsMessage,
    exceptionMessage: String,
  ) {
    try {
      val stringifiedMessage = objectMapper.writeValueAsString(hmppsMessage)
      val sendMessageRequest =
        SendMessageRequest
          .builder()
          .queueUrl(locationQueueUrl)
          .messageBody(stringifiedMessage)
          .eventTypeMessageAttributes(hmppsMessage.eventType.toString())
          .build()

      locationQueueSqsClient.sendMessage(sendMessageRequest)
    } catch (e: Exception) {
      throw MessageFailedException(exceptionMessage, e)
    }
  }
}
