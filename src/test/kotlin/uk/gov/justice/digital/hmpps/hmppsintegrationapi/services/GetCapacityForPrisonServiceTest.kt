package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.LocationsInsidePrisonGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPPrisonSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPResidentialSummary

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetCapacityForPrisonService::class],
)
class GetCapacityForPrisonServiceTest(
  @MockitoBean val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getCapacityForPrisonService: GetCapacityForPrisonService,
) : DescribeSpec(
    {
      val prisonId = "ABC"
      val filters = null
      val lipResidentialSummary =
        LIPResidentialSummary(
          prisonSummary =
            LIPPrisonSummary(
              prisonName = "Example Prison",
              workingCapacity = 800,
              signedOperationalCapacity = 1000,
              maxCapacity = 1200,
              numberOfCellLocations = 300,
            ),
          topLevelLocationType = "Residential Block",
          subLocationName = "Block A",
          parentLocation = null,
          subLocations = listOf(),
        )

      val prisonCapacity = lipResidentialSummary.prisonSummary?.toPrisonCapacity()

      beforeEach {
        Mockito.reset(consumerPrisonAccessService)
        Mockito.reset(locationsInsidePrisonGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonCapacity>(prisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        whenever(locationsInsidePrisonGateway.getResidentialSummary(prisonId)).thenReturn(Response(data = lipResidentialSummary))
      }

      it("performs a search according to prisonId and returns data") {
        val result = getCapacityForPrisonService.execute(prisonId, filters)
        result.data.shouldNotBeNull()
        result.data.shouldBe(prisonCapacity)
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if consumerPrisonAccessService returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Consumer Prison Access Service not found"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PrisonCapacity>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getCapacityForPrisonService.execute(prisonId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if locationsInsidePrisonGateway returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "locationsInsidePrisonGateway returns errors"))
        whenever(locationsInsidePrisonGateway.getResidentialSummary(prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getCapacityForPrisonService.execute(prisonId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
