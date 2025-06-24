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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityCategory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivitySchedule
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstancesForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAdvanceAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesMinimumEducationLevel
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSlot
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDateTime

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
      val prisonerId = "A1234AA"
      val prisonCode = "MKI"
      val activitiesActivityScheduledInstanceForPerson =
        listOf(
          ActivitiesActivityScheduledInstancesForPrisoner(
            id = 123456L,
            date = "2022-10-20",
            startTime = "09:00",
            endTime = "12:00",
            timeSlot = "AM",
            cancelled = false,
            attendances =
              listOf(
                ActivitiesAttendance(
                  id = 123L,
                  scheduledInstanceId = 12L,
                  prisonerNumber = "A1234AA",
                  status = "ACTIVE",
                  editable = true,
                  payable = false,
                  attendanceReason = null,
                  comment = null,
                  recordedTime = null,
                  recordedBy = null,
                  payAmount = null,
                  bonusAmount = null,
                  pieces = null,
                  issuePayment = null,
                  incentiveLevelWarningIssued = null,
                  otherAbsenceReason = null,
                  caseNoteText = null,
                  attendanceHistory = null,
                ),
              ),
            cancelledTime = null,
            cancelledBy = null,
            cancelledReason = null,
            cancelledIssuePayment = null,
            comment = null,
            previousScheduledInstanceId = null,
            previousScheduledInstanceDate = null,
            nextScheduledInstanceId = null,
            nextScheduledInstanceDate = null,
            advanceAttendances =
              listOf(
                ActivitiesAdvanceAttendance(
                  id = 123L,
                  scheduleInstanceId = 12L,
                  prisonerNumber = "A1234AA",
                  issuePayment = null,
                  payAmount = null,
                  recordedTime = null,
                  recordedBy = null,
                  attendanceHistory = null,
                ),
              ),
            activitySchedule =
              ActivitiesActivitySchedule(
                id = 13L,
                description = "Monday AM Houseblock 3",
                internalLocation = null,
                capacity = 10,
                activity =
                  ActivitiesActivity(
                    id = 123456,
                    prisonCode = "PVI",
                    attendanceRequired = false,
                    inCell = false,
                    onWing = false,
                    offWing = false,
                    pieceWork = false,
                    outsideWork = false,
                    payPerSession = "H",
                    summary = "Maths level 1",
                    description = "A basic maths course suitable for introduction to the subject",
                    category =
                      ActivitiesActivityCategory(
                        id = 1,
                        code = "LEISURE_SOCIAL",
                        name = "Leisure and social",
                        description = "Such as association, library time and social clubs, like music or art",
                      ),
                    riskLevel = "high",
                    minimumEducationLevel =
                      listOf(
                        ActivitiesMinimumEducationLevel(
                          id = 123456,
                          educationLevelCode = "Basic",
                          educationLevelDescription = "Basic",
                          studyAreaCode = "ENGLA",
                          studyAreaDescription = "English Language",
                        ),
                      ),
                    endDate = "2022-12-21",
                    capacity = 0,
                    allocated = 0,
                    createdTime = LocalDateTime.parse("2022-09-01T09:01:02"),
                    activityState = "live",
                    paid = true,
                  ),
                scheduleWeeks = 2,
                slots =
                  listOf(
                    ActivitiesSlot(
                      id = 101L,
                      timeSlot = "AM",
                      weekNumber = 1,
                      startTime = "09:00",
                      endTime = "12:00",
                      daysOfWeek = listOf("Mon", "Wed", "Fri"),
                      mondayFlag = true,
                      tuesdayFlag = false,
                      wednesdayFlag = true,
                      thursdayFlag = false,
                      fridayFlag = true,
                      saturdayFlag = false,
                      sundayFlag = false,
                    ),
                  ),
                startDate = "2024-01-15",
                endDate = "2024-07-15",
                usePrisonRegimeTime = true,
              ),
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
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns scheduled instances for prisoner") {
        mockServer.stubForGet(
          "/prisons/$prisonCode/$prisonerId/scheduled-instances",
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetActivitiesScheduledInstanceForPrisoner.json").readText(),
        )

        val result = activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data[0].id.shouldBe(activitiesActivityScheduledInstanceForPerson[0].id)
      }

      it("Returns a bad request error") {
        mockServer.stubForGet(
          "/prisons/$prisonCode/$prisonerId/scheduled-instances",
          "{}",
          HttpStatus.BAD_REQUEST,
        )

        val result = activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, null)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
