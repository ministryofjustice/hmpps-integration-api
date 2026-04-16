package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLocationByKeyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.LocationQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [LocationController::class])
@Import(WebMvcTestConfiguration::class, FeatureFlagConfig::class)
@ActiveProfiles("test")
class LocationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getLocationByKeyService: GetLocationByKeyService,
  @MockitoBean val locationQueueService: LocationQueueService,
  @Autowired val featureFlagConfig: FeatureFlagConfig,
) : DescribeSpec({
    val prisonId = "MDI"
    val basePath = "/v1/prison/$prisonId/location"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)
    val key = "A-1-001"

    beforeEach {
      Mockito.reset(auditService)
    }

    describe("GET /{key}") {
      val filters = null
      val path = "$basePath/$key"

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
      val mappedLIPResponse = lipLocation.toLocation()

      it("should return 200 when success") {
        whenever(getLocationByKeyService.execute(prisonId, key, filters)).thenReturn(Response(data = mappedLIPResponse))

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsJson<Response<Location>>().shouldBe(Response(data = mappedLIPResponse))
      }

      it("should call the audit service") {
        whenever(getLocationByKeyService.execute(prisonId, key, filters)).thenReturn(Response(data = mappedLIPResponse))

        mockMvc.performAuthorised(path)
        verify(
          auditService,
          times(1),
        ).createEvent(
          "GET_LOCATION_INFORMATION",
          mapOf("prisonId" to prisonId, "key" to key),
        )
      }

      it("returns 400 when invalid params") {
        val invalidParam = "ABC123"
        val invalidPath = "$basePath/$invalidParam"
        whenever(getLocationByKeyService.execute(prisonId, invalidParam, filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised(invalidPath)
        result.response.status.shouldBe(400)
      }

      it("returns 404 when getLocationByKeyService returns not found") {
        whenever(getLocationByKeyService.execute(prisonId, key, filters)).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(404)
      }
    }

    describe("POST /{key}/deactivate") {
      val path = "$basePath/$key/deactivate"
      val who = "automated-test-client"
      val deactivateLocationRequest =
        DeactivateLocationRequest(
          deactivationReason = DeactivationReason.DAMAGED,
          deactivationReasonDescription = "Scheduled maintenance",
          proposedReactivationDate = LocalDate.now(),
        )

      it("returns 200 when location is successfully deactivated") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, who, null)).thenReturn(
          Response(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(200)
        result.response.contentAsJson<DataResponse<HmppsMessageResponse>>().shouldBe(
          DataResponse(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )
      }

      it("returns 404 when location is not found") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, who, null)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Location not found", causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON))),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(404)
      }

      it("returns 400 when invalid request data is provided") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, who, null)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid data", causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON))),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(400)
      }

      it("returns 409 when prisoner is in cell") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, who, null)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.CONFLICT, description = "Prisoner in cell", causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH))),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(409)
        result.response.contentAsString.contains("Prisoner in cell")
      }

      it("logs audit event when location is deactivated") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, who, null)).thenReturn(
          Response(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )

        mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        verify(auditService, times(1)).createEvent(
          "DEACTIVATE_LOCATION",
          mapOf(
            "prisonId" to prisonId,
            "key" to key,
          ),
        )
      }
    }
  })
