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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesActivityScheduledInstanceForPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import java.time.LocalDate
import java.time.LocalTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetScheduledInstancesForPrisonerService::class],
)
class GetScheduledInstancesForPrisonerServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val getPersonService: GetPersonService,
  val getScheduledInstancesForPrisonerService: GetScheduledInstancesForPrisonerService,
) : DescribeSpec(
    {
      val prisonCode = "MKI"
      val prisonerId = "A1234AA"
      val filters = ConsumerFilters(prisons = listOf(prisonCode))
      val activitiesActivityScheduledInstanceForPerson =
        listOf(
          ActivitiesActivityScheduledInstanceForPrisoner(
            scheduledInstanceId = 123456L,
            allocationId = 1L,
            prisonCode = prisonCode,
            sessionDate = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            endTime = LocalTime.of(12, 0),
            prisonerNumber = prisonerId,
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

      val activityScheduledInstanceForPerson = activitiesActivityScheduledInstanceForPerson.map { it.toActivityScheduledInstanceForPrisoner() }

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = emptyList()))
        whenever(getPersonService.getNomisNumberWithPrisonFilter(prisonerId, filters)).thenReturn(Response(data = NomisNumber(prisonerId)))
      }

      it("should return scheduled instances for a prisoner") {
        whenever(activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null)).thenReturn(Response(data = activitiesActivityScheduledInstanceForPerson))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, filters)
        result.data.shouldBe(activityScheduledInstanceForPerson)
        result.errors.shouldBeEmpty()
      }

      it("should return an error if consumer filter check fails") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.ACTIVITIES,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from consumer prison access check",
            ),
          )

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters, upstreamServiceType = UpstreamApi.ACTIVITIES)).thenReturn(Response(data = null, errors = errors))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }

      it("should return an error if getPersonService returns an error") {
        val errors =
          listOf(
            UpstreamApiError(
              causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
              type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              description = "Error from getPersonService",
            ),
          )
        whenever(getPersonService.getNomisNumberWithPrisonFilter(prisonerId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
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
        whenever(activitiesGateway.getScheduledInstancesForPrisoner(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null)).thenReturn(Response(data = null, errors = errors))

        val result = getScheduledInstancesForPrisonerService.execute(prisonCode, prisonerId, "2022-09-10", "2023-09-10", null, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
