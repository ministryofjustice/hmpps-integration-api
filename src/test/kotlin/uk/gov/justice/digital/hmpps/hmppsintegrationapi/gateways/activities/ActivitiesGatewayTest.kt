package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class ActivitiesGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val objectMapper = jacksonObjectMapper()
      val prisonCode = "MDI"
      val pathNoParams = "/scheduled-events/prison/$prisonCode"
      val activitiesApiMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val prisonerNumbers = listOf("A8451DY", "A8452DY", "A8650DY", "A8633DY")
      val requestBodyMap = mapOf("prisonerNumbers" to prisonerNumbers)

      val jsonRequest = objectMapper.writeValueAsString(requestBodyMap)

      val responseJson = """
        {
          "prisonCode": "MDI",
          "prisonerNumbers": ["A8451DY", "A8452DY", "A8650DY", "A8633DY"],
          "startDate": "2022-11-01",
          "endDate": "2022-11-28",
          "appointments": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ],
          "courtHearings": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ],
          "visits": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ],
          "activities": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ],
          "externalTransfers": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ],
          "adjudications": [
            {
              "prisonCode": "MDI",
              "eventSource": "NOMIS",
              "eventType": "APPOINTMENT",
              "scheduledInstanceId": 9999,
              "appointmentSeriesId": 9999,
              "appointmentId": 9999,
              "appointmentAttendeeId": 9999,
              "oicHearingId": 9999,
              "eventId": 10001,
              "bookingId": 10001,
              "internalLocationId": 10001,
              "internalLocationCode": "5-A-SIDE COM",
              "internalLocationUserDescription": "GYM ORDERLY",
              "internalLocationDescription": "MDI-GYM-5-A-SIDE COM",
              "categoryCode": "GOVE",
              "categoryDescription": "Governor",
              "summary": "Court hearing",
              "comments": "Reception for 8am please.",
              "inCell": false,
              "onWing": false,
              "offWing": false,
              "outsidePrison": false,
              "cancelled": false,
              "suspended": false,
              "autoSuspended": false,
              "prisonerNumber": "GF10101",
              "date": "2022-09-30",
              "startTime": "09:00",
              "endTime": "10:00",
              "priority": 0,
              "appointmentSeriesCancellationStartDate": "2025-06-10",
              "appointmentSeriesCancellationStartTime": "10:30",
              "appointmentSeriesFrequency": "DAILY",
              "paidActivity": false,
              "issuePayment": true,
              "attendanceStatus": "WAITING",
              "attendanceReasonCode": "SICK"
            }
          ]
        }
        """
      beforeEach {
        activitiesApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        activitiesApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers)

        verify(hmppsAuthGateway, times(1))
          .getClientToken("ACTIVITIES")
      }

      it("upstream API returns an error, throw exception") {
        activitiesApiMockServer.stubForPost(pathNoParams, jsonRequest, "", HttpStatus.BAD_REQUEST)
        val response =
          shouldThrow<WebClientResponseException> {
            activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers)
          }
        response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
      }

      it("upstream API returns an forbidden error, throw forbidden exception") {
        activitiesApiMockServer.stubForPost(pathNoParams, jsonRequest, "", HttpStatus.FORBIDDEN)
        val response = activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.FORBIDDEN)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.ACTIVITIES)
      }

      it("returns scheduled events") {
        activitiesApiMockServer.stubForPost(pathNoParams, jsonRequest, responseJson, HttpStatus.OK)
        val response = activitiesGateway.getScheduledEvents(prisonCode, prisonerNumbers)
        response.data!!.prisonCode.shouldBe(prisonCode)
      }
    },
  )
