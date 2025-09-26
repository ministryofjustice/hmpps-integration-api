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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesHistoricalAttendance
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Person
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
  @MockitoBean val getPersonService: GetPersonService,
  val getHistoricalAttendancesService: GetHistoricalAttendancesService,
) : DescribeSpec(
    {
      val prisonerNumber = "A1234AA"
      val prisonId = "MDI"
      val startDate = "2023-09-10"
      val endDate = "2023-10-10"
      val filters = ConsumerFilters(listOf(prisonId))
      val person =
        Person(
          firstName = "John",
          lastName = "Morgan",
        )

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
        Mockito.reset(getPersonService, activitiesGateway)

        whenever(getPersonService.getPersonWithPrisonFilter(prisonerNumber, filters)).thenReturn(Response(data = person))
      }

      it("Returns historical attendances") {
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonId)).thenReturn(Response(data = listOf(activitiesHistoricalAttendance)))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonId, filters)
        result.data.shouldBe(listOf(activitiesHistoricalAttendance.toHistoricalAttendance()))
        result.errors.shouldBeEmpty()
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
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonId)).thenReturn(Response(data = listOf(activitiesHistoricalAttendance)))
        whenever(getPersonService.getPersonWithPrisonFilter(prisonerNumber, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonId, filters)
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
        whenever(activitiesGateway.getHistoricalAttendances(prisonerNumber, startDate, endDate, prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getHistoricalAttendancesService.execute(prisonerNumber, startDate, endDate, prisonId, filters)
        result.data.shouldBeNull()
        result.errors.shouldBe(errors)
      }
    },
  )
