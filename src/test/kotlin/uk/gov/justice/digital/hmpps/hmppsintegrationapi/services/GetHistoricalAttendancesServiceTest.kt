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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesHistoricalAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetHistoricalAttendancesService::class],
)
class GetHistoricalAttendancesServiceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  val getHistoricalAttendancesService: GetHistoricalAttendancesService,
) : DescribeSpec(
    {
      val prisonerNumber = "A1234AA"
      val startDate = "2023-09-10"
      val endDate = "2023-10-10"
      val prisonCode = "MDI"
      val filters = ConsumerFilters(prisons = listOf(prisonCode))
      val activitiesHistoricalAttendance =
        ActivitiesHistoricalAttendance(
          id = 1L,
          scheduleInstanceId = 1001L,
          prisonerNumber = "A1234AA",
          status = "WAITING",
          editable = true,
          payable = true,
        )

      beforeEach {
        Mockito.reset(consumerPrisonAccessService, activitiesGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters)).thenReturn(Response(data = null, errors = emptyList()))
      }

      it("Returns historical attendances") {
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonCode)).thenReturn(Response(data = listOf(activitiesHistoricalAttendance)))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonCode, filters)
        result.data.shouldBe(listOf(activitiesHistoricalAttendance.toHistoricalAttendance()))
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
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonCode)).thenReturn(Response(data = listOf(activitiesHistoricalAttendance)))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Any>(prisonCode, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonCode, filters)
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
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonCode)).thenReturn(Response(data = null, errors = errors))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonCode, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
