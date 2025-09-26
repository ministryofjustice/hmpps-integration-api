import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate
import java.time.LocalTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetScheduledInstancesForPrisonerGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val prisonCode = "MKI"
      val prisonerNumber = "A1234AA"
      val activitiesActivityScheduledInstanceForPerson =
        listOf(
          ActivitiesActivityScheduledInstanceForPrisoner(
            scheduledInstanceId = 1,
            allocationId = 1L,
            prisonCode = prisonCode,
            sessionDate = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(12, 0),
            prisonerNumber = prisonerNumber,
            bookingId = 123456L,
            inCell = false,
            onWing = false,
            offWing = false,
            activityId = 1,
            activityCategory = "Activity category",
            activitySummary = "Activity summary",
            timeSlot = "AM",
            attendanceStatus = "CONFIRMED",
            paidActivity = true,
            possibleAdvanceAttendance = false,
          ),
        )

      beforeEach {
        mockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterEach {
        mockServer.stop()
        mockServer.resetValidator()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerNumber, "2022-09-10", "2023-09-10", null)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns scheduled instances for prisoner") {
        mockServer.stubForGet(
          "/integration-api/prisons/$prisonCode/$prisonerNumber/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10",
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesScheduledInstanceForPrisoner.json").readText(),
        )

        val result = activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerNumber, "2022-09-10", "2023-09-10", null)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data[0].scheduledInstanceId.shouldBe(activitiesActivityScheduledInstanceForPerson[0].scheduledInstanceId)

        mockServer.assertValidationPassed()
      }

      it("Returns a bad request error") {
        mockServer.stubForGet(
          "/integration-api/prisons/$prisonCode/$prisonerNumber/scheduled-instances?startDate=2022-09-10&endDate=2023-09-10",
          "{}",
          HttpStatus.BAD_REQUEST,
        )

        val result = activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerNumber, "2022-09-10", "2023-09-10", null)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
