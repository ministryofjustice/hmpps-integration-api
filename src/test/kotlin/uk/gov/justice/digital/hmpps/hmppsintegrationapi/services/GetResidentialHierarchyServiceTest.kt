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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialHierarchyItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPResidentialHierarchyItem

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetResidentialHierarchyService::class],
)
class GetResidentialHierarchyServiceTest(
  @MockitoBean val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getResidentialHierarchyService: GetResidentialHierarchyService,
) : DescribeSpec(
    {
      val prisonId = "ABC"
      val filters = null
      val subLocation1 =
        LIPResidentialHierarchyItem(
          locationId = "sub-location-1",
          locationType = "CELL",
          locationCode = "CELL-001",
          fullLocationPath = "MDI-A-1-001",
          localName = "Cell 001",
          level = 3,
          subLocations = null,
        )
      val mainLocation =
        LIPResidentialHierarchyItem(
          locationId = "main-location-1",
          locationType = "WING",
          locationCode = "WING-A",
          fullLocationPath = "MDI-A",
          localName = "Wing A",
          level = 2,
          subLocations = listOf(subLocation1),
        )

      beforeEach {
        Mockito.reset(consumerPrisonAccessService)
        Mockito.reset(locationsInsidePrisonGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ResidentialHierarchyItem>>(prisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        whenever(locationsInsidePrisonGateway.getResidentialHierarchy(prisonId)).thenReturn(Response(data = listOf(mainLocation)))
      }

      it("performs a search according to prisonId and returns data") {
        val result = getResidentialHierarchyService.execute(prisonId, filters)
        result.data.shouldNotBeNull()
        result.data.shouldBe(listOf(mainLocation.toResidentialHierarchyItem()))
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if consumerPrisonAccessService returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Consumer Prison Access Service not found"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ResidentialHierarchyItem>>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getResidentialHierarchyService.execute(prisonId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if locationsInsidePrisonGateway returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "locationsInsidePrisonGateway returns errors"))
        whenever(locationsInsidePrisonGateway.getResidentialHierarchy(prisonId)).thenReturn(Response(data = null, errors = errors))

        val result = getResidentialHierarchyService.execute(prisonId, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
