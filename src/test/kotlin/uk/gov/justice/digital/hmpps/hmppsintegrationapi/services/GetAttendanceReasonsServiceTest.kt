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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesAttendanceReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetAttendanceReasonsService::class],
)
class GetAttendanceReasonsServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  val getAttendanceReasonsService: GetAttendanceReasonsService,
) : DescribeSpec(
    {
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

      beforeEach {
        Mockito.reset(activitiesGateway)
      }

      it("should return an attendance reason") {
        whenever(activitiesGateway.getAttendanceReasons()).thenReturn(Response(data = listOf(activitiesAttendanceReason)))

        val result = getAttendanceReasonsService.execute()
        result.data.shouldBe(listOf(activitiesAttendanceReason.toReasonForAttendance()))
        result.errors.shouldBeEmpty()
      }

      it("should return an error if gateway returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.FORBIDDEN,
              description = "Error from gateway",
            ),
          )
        whenever(activitiesGateway.getAttendanceReasons()).thenReturn(Response(data = null, errors = errors))

        val result = getAttendanceReasonsService.execute()
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
