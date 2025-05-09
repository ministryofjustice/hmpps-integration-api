package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.exception.EntityNotFoundException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessage
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.hmpps.sqs.HmppsQueue
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import java.time.LocalDate
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [LocationQueueService::class],
)
internal class LocationQueueServiceTest(
  private val locationQueueService: LocationQueueService,
  @MockitoBean val hmppsQueueService: HmppsQueueService,
  @MockitoBean val objectMapper: ObjectMapper,
  @MockitoBean val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
) : DescribeSpec(
    {
      val mockSqsClient = mock<SqsAsyncClient>()
      val locationQueue =
        mock<HmppsQueue> {
          on { sqsClient } doReturn mockSqsClient
          on { queueUrl } doReturn "https://test-queue-url"
        }

      val deactivateLocationRequest =
        DeactivateLocationRequest(
          deactivationReason = DeactivationReason.DAMAGED,
          deactivationReasonDescription = "Smashed window",
          proposedReactivationDate = LocalDate.now().plusDays(30),
          planetFmReference = "23423TH/5",
        )
      val prisonId = "MDI"
      val key = "MDI-123"
      val filters = ConsumerFilters(prisons = listOf(prisonId))
      val lipLocation =
        LIPLocation(
          prisonId = prisonId,
          id = "123",
          code = "LOC123",
          pathHierarchy = "MDI/LOC123",
          locationType = "CELL",
          permanentlyInactive = false,
          active = true,
          deactivatedByParent = false,
          topLevelId = "MDI",
          level = 1,
          leafLevel = true,
          lastModifiedBy = "admin",
          lastModifiedDate = LocalDateTime.now(),
          key = "MDI-LOC123",
          isResidential = true,
        )

      beforeTest {
        reset(mockSqsClient, objectMapper)

        whenever(hmppsQueueService.findByQueueId("location")).thenReturn(locationQueue)
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
          .thenReturn(Response(data = null, errors = emptyList()))
        whenever(locationsInsidePrisonGateway.getLocationByKey(key)).thenReturn(Response(data = lipLocation))
      }

      it("should send deactivate location request successfully when all conditions are met") {
        val result = locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, filters)

        result.data?.message.shouldBe("Deactivate location written to queue")
        result.errors.shouldBeEmpty()
      }

      it("successfully adds to message queue") {
        val messageBody = """{"messageId":"1","eventType":"LocationDeactivate","messageAttributes":{}}"""
        whenever(objectMapper.writeValueAsString(any<HmppsMessage>())).thenReturn(messageBody)

        val response = locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, filters)

        verify(mockSqsClient).sendMessage(
          argThat<SendMessageRequest> { request: SendMessageRequest? ->
            request?.queueUrl() == "https://test-queue-url" &&
              request.messageBody() == messageBody
          },
        )

        response.data.shouldBeTypeOf<HmppsMessageResponse>()
      }

      it("should return errors when consumer does not have access to the prison") {
        val accessError = UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<HmppsMessageResponse>(prisonId, filters))
          .thenReturn(Response(data = null, errors = listOf(accessError)))

        val result = locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, filters)

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(accessError))
      }

      it("should throw EntityNotFoundException when location is not found for the key") {
        whenever(locationsInsidePrisonGateway.getLocationByKey(key)).thenReturn(Response(data = null))

        val exception =
          shouldThrow<EntityNotFoundException> {
            locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, filters)
          }
        exception.message.shouldBe("Location not found for key in upstream: $key")
      }

      it("should return errors when location gateway returns errors") {
        val locationError = UpstreamApiError(UpstreamApi.PRISON_API, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Location not found")
        whenever(locationsInsidePrisonGateway.getLocationByKey(key))
          .thenReturn(Response(data = null, errors = listOf(locationError)))

        val result = locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, filters)

        result.data.shouldBe(null)
        result.errors.shouldBe(listOf(locationError))
      }
    },
  )
