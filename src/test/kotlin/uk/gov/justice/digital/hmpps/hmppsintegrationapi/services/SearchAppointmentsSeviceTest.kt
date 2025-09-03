package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAppointmentCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAppointmentInternalLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendee
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDate

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [SearchAppointmentsService::class],
)
internal class SearchAppointmentsSeviceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val searchAppointmentsService: SearchAppointmentsService,
) : DescribeSpec({
    val prisonId = "ABC"
    val filters = ConsumerFilters(null)
    val exampleRequest =
      AppointmentSearchRequest(
        appointmentType = "INDIVIDUAL",
        startDate = LocalDate.parse("2025-01-01"),
        endDate = LocalDate.parse("2025-01-31"),
        timeSlots = listOf("AM", "PM"),
        categoryCode = "GYMW",
        inCell = false,
        prisonerNumbers = listOf("A1234AA"),
      )
    val exampleGatewayResponse =
      listOf(
        ActivitiesAppointmentDetails(
          appointmentSeriesId = 12345,
          appointmentId = 123456,
          appointmentType = "INDIVIDUAL",
          prisonCode = prisonId,
          appointmentName = "string",
          attendees =
            listOf(
              ActivitiesAttendee(
                appointmentAttendeeId = 123456,
                prisonerNumber = "A1234AA",
                bookingId = 456,
              ),
            ),
          category =
            ActivitiesAppointmentCategory(
              code = "CHAP",
              description = "Chaplaincy",
            ),
          customName = "Meeting with the governor",
          internalLocation =
            ActivitiesAppointmentInternalLocation(
              id = 27,
              prisonCode = prisonId,
              description = "Chapel",
            ),
          inCell = false,
          startDate = "2025-06-12",
          startTime = "13:00",
          endTime = "13:30",
          timeSlot = "PM",
          isRepeat = false,
          sequenceNumber = 3,
          maxSequenceNumber = 6,
          isEdited = false,
          isCancelled = false,
          isExpired = false,
          createdTime = "2025-06-12T08:03:06.917Z",
          updatedTime = "2025-06-12T08:03:06.917Z",
          cancelledTime = "2025-06-12T08:03:06.917Z",
          cancelledBy = "A7891B",
        ),
      )

    beforeEach {
      Mockito.reset(consumerPrisonAccessService, activitiesGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<AppointmentDetails>>(prisonId, filters, UpstreamApi.ACTIVITIES)).thenReturn(
        Response(data = null),
      )

      whenever(
        activitiesGateway.getAppointments(
          prisonId,
          exampleRequest,
        ),
      ).thenReturn(
        Response(
          data = exampleGatewayResponse,
        ),
      )
    }

    it("Posts a appointments search") {
      searchAppointmentsService.execute(
        prisonId,
        exampleRequest,
        filters,
      )

      verify(activitiesGateway, VerificationModeFactory.times(1)).getAppointments(
        prisonId,
        exampleRequest,
      )
    }

    it("posts an appointments search and receives expected response object") {
      val result =
        searchAppointmentsService.execute(
          prisonId,
          exampleRequest,
          filters,
        )

      result.data.shouldBe(exampleGatewayResponse.map { it.toAppointmentDetails() })
    }

    it("records upstream API errors") {
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<AppointmentDetails>>(prisonId, filters, UpstreamApi.ACTIVITIES)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ACTIVITIES,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )
      val response =
        searchAppointmentsService.execute(
          prisonId,
          exampleRequest,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.ACTIVITIES,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("records upstream API errors gateway returns error") {
      whenever(
        activitiesGateway.getAppointments(
          prisonId,
          exampleRequest,
        ),
      ).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                type = UpstreamApiError.Type.BAD_REQUEST,
                causedBy = UpstreamApi.ACTIVITIES,
              ),
            ),
        ),
      )
      val response =
        searchAppointmentsService.execute(
          prisonId,
          exampleRequest,
          filters,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.ACTIVITIES,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
