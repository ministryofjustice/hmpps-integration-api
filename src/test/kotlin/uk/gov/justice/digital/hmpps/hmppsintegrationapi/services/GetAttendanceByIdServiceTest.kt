package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendanceHistory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendanceReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAttendanceByIdService::class],
)
class GetAttendanceByIdServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getAttendanceByIdService: GetAttendanceByIdService,
) : DescribeSpec(
    {
      val filters = RoleFilters(prisons = listOf("MDI"))
      val prisonerNumber = "A1234AA"
      val attendanceId = 123456L
      val activitiesAttendanceReason =
        ActivitiesAttendanceReason(
          id = 123456,
          code = "SICK",
          description = "Sick",
          attended = true,
          capturePay = true,
          captureMoreDetail = true,
          captureCaseNote = true,
          captureIncentiveLevelWarning = false,
          captureOtherText = false,
          displayInAbsence = false,
          displaySequence = 1,
          notes = "Maps to ACCAB in NOMIS",
        )
      val activitiesAttendance =
        ActivitiesAttendance(
          id = attendanceId,
          scheduledInstanceId = 123456,
          prisonerNumber = prisonerNumber,
          attendanceReason = activitiesAttendanceReason,
          comment = "Prisoner was too unwell to attend the activity.",
          recordedTime = "2023-09-10T09:30:00",
          recordedBy = "A.JONES",
          status = "WAITING",
          payAmount = 100,
          bonusAmount = 50,
          pieces = 0,
          issuePayment = true,
          incentiveLevelWarningIssued = true,
          otherAbsenceReason = "Prisoner has a valid reason to miss the activity.",
          caseNoteText = "Prisoner has refused to attend the activity without a valid reason to miss the activity.",
          attendanceHistory =
            listOf(
              ActivitiesAttendanceHistory(
                id = 123456,
                attendanceReason = activitiesAttendanceReason,
                comment = "Prisoner was too unwell to attend the activity.",
                recordedTime = "2023-09-10T09:30:00",
                recordedBy = "A.JONES",
                issuePayment = true,
                incentiveLevelWarningIssued = true,
                otherAbsenceReason = "Prisoner has a valid reason to miss the activity.",
                caseNoteText = "Prisoner has refused to attend the activity without a valid reason to miss the activity.",
              ),
            ),
          editable = true,
          payable = true,
        )
      val persona = personInProbationAndNomisPersona
      val person = Person(firstName = persona.firstName, lastName = persona.lastName, identifiers = persona.identifiers)

      beforeEach {
        Mockito.reset(activitiesGateway, getPersonService)
      }

      it("should return an attendance record") {
        whenever(activitiesGateway.getAttendanceById(attendanceId)).thenReturn(Response(data = activitiesAttendance))
        whenever(getPersonService.getPersonWithPrisonFilter(prisonerNumber, filters)).thenReturn(Response(data = person))

        val result = getAttendanceByIdService.execute(attendanceId, filters)
        result.data.shouldBe(activitiesAttendance.toAttendance())
        result.errors.shouldBeEmpty()
      }

      it("should return an error if gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from gateway",
            ),
          )
        whenever(activitiesGateway.getAttendanceById(attendanceId)).thenReturn(Response(data = null, errors = errors))

        val result = getAttendanceByIdService.execute(attendanceId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }

      it("should return an error if gateway returns no data") {
        whenever(activitiesGateway.getAttendanceById(attendanceId)).thenReturn(Response(data = null))

        val result = getAttendanceByIdService.execute(attendanceId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
            ),
          ),
        )
      }

      it("should return an error if getPersonService returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISON_API,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from getPersonService",
            ),
          )
        whenever(activitiesGateway.getAttendanceById(attendanceId)).thenReturn(Response(data = activitiesAttendance))
        whenever(getPersonService.getPersonWithPrisonFilter(prisonerNumber, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getAttendanceByIdService.execute(attendanceId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
