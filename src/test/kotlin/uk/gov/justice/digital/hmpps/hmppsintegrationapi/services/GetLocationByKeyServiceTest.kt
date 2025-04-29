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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLocationByKeyService::class],
)
class GetLocationByKeyServiceTest(
  @MockitoBean val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getLocationByKeyService: GetLocationByKeyService,
) : DescribeSpec(
    {
      val prisonId = "MDI"
      val key = "A-1-001"
      val gatewayKey = "MDI-A-1-001"
      val filters = null

      val lipLocation =
        LIPLocation(
          id = "123",
          prisonId = "MDI",
          code = "001",
          pathHierarchy = "A-1-001",
          locationType = "CELL",
          localName = "Wing A",
          comments = "Standard cell",
          permanentlyInactive = false,
          permanentlyInactiveReason = null,
          capacity = null,
          oldWorkingCapacity = null,
          certification = null,
          usage = null,
          accommodationTypes = listOf("NORMAL_ACCOMMODATION"),
          specialistCellTypes = listOf("ACCESSIBLE_CELL"),
          usedFor = listOf("STANDARD_ACCOMMODATION"),
          status = "ACTIVE",
          convertedCellType = null,
          otherConvertedCellType = null,
          active = true,
          deactivatedByParent = false,
          deactivatedDate = "2023-01-23T12:23:00",
          deactivatedReason = null,
          deactivationReasonDescription = null,
          deactivatedBy = null,
          proposedReactivationDate = "2026-01-24",
          planetFmReference = "2323/45M",
          topLevelId = "MDI-A",
          level = 1,
          leafLevel = true,
          parentId = "MDI-A-1",
          parentLocation = null,
          inactiveCells = 0,
          numberOfCellLocations = 1,
          childLocations = null,
          changeHistory = null,
          transactionHistory = null,
          lastModifiedBy = "USER1",
          lastModifiedDate = LocalDateTime.now(),
          key = "MDI-A-1-001",
          isResidential = true,
        )
      beforeEach {
        Mockito.reset(consumerPrisonAccessService)
        Mockito.reset(locationsInsidePrisonGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Location?>(prisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        whenever(locationsInsidePrisonGateway.getLocationByKey(gatewayKey)).thenReturn(Response(data = lipLocation))
      }

      it("performs a search according to prisonId and returns data") {
        val result = getLocationByKeyService.execute(prisonId, filters, key)
        result.data.shouldNotBeNull()
        result.data.shouldBe(lipLocation.toLocation())
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if consumerPrisonAccessService returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Consumer Prison Access Service not found"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<Location?>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getLocationByKeyService.execute(prisonId, filters, key)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if locationsInsidePrisonGateway returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "locationsInsidePrisonGateway returns errors"))
        whenever(locationsInsidePrisonGateway.getLocationByKey(gatewayKey)).thenReturn(Response(data = null, errors = errors))

        val result = getLocationByKeyService.execute(prisonId, filters, key)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
