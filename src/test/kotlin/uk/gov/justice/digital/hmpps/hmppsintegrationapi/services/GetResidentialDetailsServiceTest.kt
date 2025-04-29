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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocationCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocationCertification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPPrisonSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPResidentialSummary
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetResidentialDetailsService::class],
)
class GetResidentialDetailsServiceTest(
  @MockitoBean val locationsInsidePrisonGateway: LocationsInsidePrisonGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getResidentialDetailsService: GetResidentialDetailsService,
) : DescribeSpec(
    {
      val prisonId = "MKI"
      val parentPathHierarchy = null
      val filters = null
      val residentialSummary =
        LIPResidentialSummary(
          prisonSummary =
            LIPPrisonSummary(
              prisonName = "Millsike (HMP)",
              workingCapacity = 1,
              signedOperationalCapacity = 0,
              maxCapacity = 1532,
              numberOfCellLocations = 419,
            ),
          topLevelLocationType = "Wings",
          subLocationName = "Wings",
          locationHierarchy = null,
          parentLocation =
            LIPLocation(
              id = "01951953-2f87-7f26-a803-e8157a95e5b5",
              prisonId = "MKI",
              code = "A",
              pathHierarchy = "A",
              locationType = "WING",
              localName = "MKI-A",
              permanentlyInactive = false,
              capacity =
                LIPLocationCapacity(
                  maxCapacity = 240,
                  workingCapacity = 1,
                ),
              certification =
                LIPLocationCertification(
                  certified = true,
                  capacityOfCertifiedCell = 240,
                ),
              accommodationTypes = listOf("NORMAL_ACCOMMODATION"),
              specialistCellTypes = listOf("ACCESSIBLE_CELL", "CONSTANT_SUPERVISION"),
              usedFor = listOf("STANDARD_ACCOMMODATION"),
              status = "ACTIVE",
              active = true,
              deactivatedByParent = false,
              topLevelId = "01951953-2f87-7f26-a803-e8157a95e5b5",
              level = 1,
              leafLevel = false,
              inactiveCells = 221,
              numberOfCellLocations = 222,
              lastModifiedBy = "JCARTWRIGHT_GEN",
              lastModifiedDate = LocalDateTime.parse("2025-04-02T10:56:45"),
              key = "MKI-A",
              isResidential = true,
            ),
          subLocations = listOf(),
        )

      beforeEach {
        Mockito.reset(consumerPrisonAccessService)
        Mockito.reset(locationsInsidePrisonGateway)

        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ResidentialHierarchyItem>>(prisonId, filters)).thenReturn(Response(data = null, errors = emptyList()))
        whenever(locationsInsidePrisonGateway.getResidentialSummary(prisonId, parentPathHierarchy)).thenReturn(Response(data = residentialSummary))
      }

      it("performs a search according to prisonId and parentPathHierarchy and returns data") {
        val result = getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)
        result.data.shouldNotBeNull()
        result.data.shouldBe(residentialSummary.toResidentialDetails())
        result.errors.count().shouldBe(0)
      }

      it("should return a list of errors if consumerPrisonAccessService returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Consumer Prison Access Service not found"))
        whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<ResidentialHierarchyItem>>(prisonId, filters)).thenReturn(Response(data = null, errors = errors))

        val result = getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }

      it("should return a list of errors if locationsInsidePrisonGateway returns errors") {
        val errors = listOf(UpstreamApiError(UpstreamApi.LOCATIONS_INSIDE_PRISON, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "locationsInsidePrisonGateway returns errors"))
        whenever(locationsInsidePrisonGateway.getResidentialSummary(prisonId, parentPathHierarchy)).thenReturn(Response(data = null, errors = errors))

        val result = getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)
        result.data.shouldBe(null)
        result.errors.shouldBe(errors)
      }
    },
  )
