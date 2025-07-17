package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.AppointmentSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetAppointmentsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val objectMapper = jacksonObjectMapper()
      val prisonCode = "MDI"
      val path = "/integration-api/appointments/$prisonCode/search"
      val activitiesApiMockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val appointmentSearchRequest =
        AppointmentSearchRequest(
          appointmentType = "INDIVIDUAL",
          startDate = LocalDate.parse("2025-01-01"),
          endDate = LocalDate.parse("2025-01-31"),
          timeSlots = listOf("AM", "PM"),
          categoryCode = "GYMW",
          inCell = false,
          prisonerNumbers = listOf("A1234AA"),
        )

      val jsonRequest = objectMapper.writeValueAsString(appointmentSearchRequest.toApiConformingMap())

      beforeEach {
        activitiesApiMockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        activitiesApiMockServer.stop()
        activitiesApiMockServer.resetValidator()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getAppointments(prisonCode, appointmentSearchRequest)

        verify(hmppsAuthGateway, times(1))
          .getClientToken("ACTIVITIES")
      }

      it("upstream API returns a bad request error, throw exception") {
        activitiesApiMockServer.stubForPost(path, jsonRequest, "", HttpStatus.BAD_REQUEST)
        val result = activitiesGateway.getAppointments(prisonCode, appointmentSearchRequest)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }

      it("returns appointments") {
        activitiesApiMockServer.stubForPost(path, jsonRequest, File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetAppointments.json").readText(), HttpStatus.OK)
        val response = activitiesGateway.getAppointments(prisonCode, appointmentSearchRequest)
        response.data!![0].appointmentId.shouldBe(123456)

        activitiesApiMockServer.assertValidationPassed()
      }

      it("does not serialize endDate when null to avoid sending 'null' as string") {
        val request =
          AppointmentSearchRequest(
            startDate = LocalDate.parse("2025-06-16"),
          )

        val generatedJson = objectMapper.writeValueAsString(request.toApiConformingMap())

        generatedJson.contains("endDate") shouldBe false
      }
    },
  )
