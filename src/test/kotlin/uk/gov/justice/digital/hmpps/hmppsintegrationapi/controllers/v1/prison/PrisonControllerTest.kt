package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DataResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivateLocationRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DeactivationReason
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.HmppsMessageResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCertification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialHierarchyItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPPrisonSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCapacityForPrisonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetLocationByKeyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetResidentialDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetResidentialHierarchyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.LocationQueueService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [PrisonController::class])
@ActiveProfiles("test")
internal class PrisonControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @Autowired val request: HttpServletRequest,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonersService: GetPrisonersService,
  @MockitoBean val getVisitsService: GetVisitsService,
  @MockitoBean val getResidentialHierarchyService: GetResidentialHierarchyService,
  @MockitoBean val getLocationByKeyService: GetLocationByKeyService,
  @MockitoBean val getResidentialDetailsService: GetResidentialDetailsService,
  @MockitoBean val getCapacityForPrisonService: GetCapacityForPrisonService,
  @MockitoBean val locationQueueService: LocationQueueService,
) : DescribeSpec({
    val hmppsId = "200313116M"
    val basePath = "/v1/prison"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    afterTest {
      Mockito.reset(getPersonService)
      Mockito.reset(auditService)
      Mockito.reset(getPrisonersService)
    }

    describe("GET /prisoners/{hmppsId}") {
      it("returns 500 when service throws an exception") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenThrow(RuntimeException("Service error"))

        val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        result.response.status.shouldBe(500)
      }

      it("returns a person with all fields populated") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenReturn(
          Response(
            data =
              PersonInPrison(
                firstName = "Barry",
                lastName = "Allen",
                middleName = "Jonas",
                dateOfBirth = LocalDate.parse("2023-03-01"),
                gender = "Male",
                ethnicity = "Caucasian",
                pncId = "PNC123456",
                category = "C",
                csra = "HIGH",
                receptionDate = "2023-05-01",
                status = "ACTIVE IN",
                prisonId = "MDI",
                prisonName = "HMP Leeds",
                cellLocation = "A-1-002",
                youthOffender = false,
              ),
          ),
        )

        val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        result.response.contentAsString.shouldBe(
          """
            {
             "data":{
                   "firstName":"Barry",
                   "lastName":"Allen",
                   "middleName":"Jonas",
                   "dateOfBirth":"2023-03-01",
                   "gender":"Male",
                   "ethnicity":"Caucasian",
                   "aliases":[],
                   "identifiers":{
                      "nomisNumber":null,
                      "croNumber":null,
                      "deliusCrn":null
                   },
                   "pncId": "PNC123456",
                   "category": "C",
                   "csra": "HIGH",
                   "receptionDate": "2023-05-01",
                   "status": "ACTIVE IN",
                   "prisonId": "MDI",
                   "prisonName": "HMP Leeds",
                   "cellLocation": "A-1-002",
                   "youthOffender": false
                }
             }
          """.removeWhitespaceAndNewlines(),
        )
      }

      it("logs audit event") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenReturn(
          Response(
            data =
              PersonInPrison(
                firstName = "Barry",
                lastName = "Allen",
                middleName = "Jonas",
                dateOfBirth = LocalDate.parse("2023-03-01"),
                gender = "Male",
                ethnicity = "Caucasian",
                pncId = "PNC123456",
                category = "C",
                csra = "HIGH",
                receptionDate = "2023-05-01",
                status = "ACTIVE IN",
                prisonId = "MDI",
                prisonName = "HMP Leeds",
                cellLocation = "A-1-002",
                youthOffender = false,
              ),
          ),
        )

        mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        verify(
          auditService,
          times(1),
        ).createEvent(
          "GET_PERSON_DETAILS",
          mapOf("hmppsId" to hmppsId),
        )
      }

      it("returns 200 when prisoner is not found but successful query") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenReturn(
          Response(
            data = null,
            errors = emptyList(),
          ),
        )

        val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        result.response.status.shouldBe(200)
      }

      it("returns 404 when NOMIS number is not found") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  description = "NOMIS number not found",
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        result.response.status.shouldBe(404)
      }

      it("returns 400 when HMPPS ID does not match format") {
        whenever(getPersonService.getPrisoner(eq(hmppsId), anyOrNull())).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  description = "Invalid HMPPS ID: $hmppsId",
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.PRISON_API,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised("$basePath/prisoners/$hmppsId")
        result.response.status.shouldBe(400)
      }
    }

    describe("GET /prisoners") {
      val firstName = "Barry"
      val lastName = "Allen"
      val dateOfBirth = "2023-03-01"
      val path = "$basePath/prisoners?first_name=$firstName&last_name=$lastName&date_of_birth=$dateOfBirth"

      it("returns 500 when prison/prisoners throws an unexpected error") {
        whenever(getPrisonersService.execute("Barry", "Allen", "2023-03-01", false, null)).thenThrow(RuntimeException("Service error"))

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(500)
      }

      it("returns 500 when prisoner query gets no result") {
        whenever(getPrisonersService.execute("Barry", "Allen", "2023-03-01", false, ConsumerFilters(emptyList()))).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  description = "Service error",
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(500)
      }

      it("returns a 200 OK status code") {
        whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth, false, null)).thenReturn(
          Response(
            data =
              listOf(
                PersonInPrison(
                  firstName = "Barry",
                  lastName = "Allen",
                  middleName = "Jonas",
                  dateOfBirth = LocalDate.parse("2023-03-01"),
                  youthOffender = false,
                ),
                PersonInPrison(
                  firstName = "Barry",
                  lastName = "Allen",
                  middleName = "Rock",
                  dateOfBirth = LocalDate.parse("2022-07-22"),
                  youthOffender = false,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised("$path&search_within_aliases=false")
        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("returns 403 when consumer config includes an empty prison filter field") {
        whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth, false, ConsumerFilters(prisons = emptyList()))).thenReturn(
          Response(
            data = emptyList(),
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.FORBIDDEN,
                  causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                  description = "Consumer configured with no access to any prisons",
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorisedWithCN("$path&search_within_aliases=false", "empty-prisons")
        result.response.status.shouldBe(403)
      }
    }

    describe("GET /visit/search") {
      val prisonId = "ABC"
      val path = "$basePath/$prisonId/visit/search"
      val hmppsId = "A1234AA"
      val fromDate = "2024-01-01"
      val toDate = "2024-01-14"
      val visitStatus = "BOOKED"
      val page = 1
      val size = 10
      val pathWithQueryParams = "$path?prisonId=$prisonId&visitStatus=$visitStatus&page=$page&size=$size&prisonerId=$hmppsId&fromDate=$fromDate&toDate=$toDate"
      val visitResponse =
        Visit(
          prisonerId = "PrisonerId",
          prisonId = "MDI",
          prisonName = "Some Prison",
          visitRoom = "Room",
          visitType = "Type",
          visitStatus = "Status",
          outcomeStatus = "Outcome",
          visitRestriction = "Restriction",
          startTimestamp = "Start",
          endTimestamp = "End",
          createdTimestamp = "Created",
          modifiedTimestamp = "Modified",
          firstBookedDateTime = "First",
          visitors = emptyList(),
          visitNotes = emptyList(),
          visitContact = VisitContact(name = "Name", telephone = "Telephone", email = "Email"),
          applicationReference = "dfs-wjs-abc",
          reference = "dfs-wjs-abc",
          sessionTemplateReference = "dfs-wjs-xyz",
          visitorSupport = VisitorSupport(description = "Description"),
          visitExternalSystemDetails =
            VisitExternalSystemDetails(
              clientName = "client_name",
              clientVisitReference = "12345",
            ),
        )
      val paginatedVisitsData =
        PaginatedVisits(
          content = listOf(visitResponse),
          totalCount = 1L,
          isLastPage = true,
          count = 1,
          page = 1,
          perPage = 1,
          totalPages = 1,
        )

      it("returns 200 when no errors received and with valid data") {
        whenever(getVisitsService.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
          Response(
            data = paginatedVisitsData,
          ),
        )

        val result = mockMvc.performAuthorised(pathWithQueryParams)
        result.response.status.shouldBe(200)
      }

      it("returns 200 when no errors but empty body") {
        whenever(getVisitsService.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
          Response(
            data = null,
          ),
        )

        val result = mockMvc.performAuthorised(pathWithQueryParams)
        result.response.status.shouldBe(200)
      }
      it("returns 404 when prisonId not in consumer profile") {
        whenever(getVisitsService.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  causedBy = UpstreamApi.MANAGE_PRISON_VISITS,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised(pathWithQueryParams)
        result.response.status.shouldBe(404)
      }

      it("returns 400 when invalid query parameters supplied") {
        whenever(getVisitsService.execute(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(
          Response(
            data = null,
            errors =
              listOf(
                UpstreamApiError(
                  type = UpstreamApiError.Type.BAD_REQUEST,
                  causedBy = UpstreamApi.MANAGE_PRISON_VISITS,
                ),
              ),
          ),
        )

        val result = mockMvc.performAuthorised(pathWithQueryParams)
        result.response.status.shouldBe(400)
      }
    }

    describe("GET /{prisonId}/residential-hierarchy") {
      val prisonId = "ABC"
      val includeInactive = true
      val filters = null
      val path = "$basePath/$prisonId/residential-hierarchy?includeInactive=$includeInactive"

      val subLocation =
        ResidentialHierarchyItem(
          locationId = "sub-location-1",
          locationType = "CELL",
          locationCode = "CELL-001",
          fullLocationPath = "MDI-A-1-001",
          localName = "Cell 001",
          level = 3,
          subLocations = null,
        )
      val mainLocation =
        ResidentialHierarchyItem(
          locationId = "main-location-1",
          locationType = "WING",
          locationCode = "WING-A",
          fullLocationPath = "MDI-A",
          localName = "Wing A",
          level = 2,
          subLocations = listOf(subLocation),
        )

      beforeEach {
        Mockito.reset(getResidentialHierarchyService)
      }

      it("should return 200 when success") {
        whenever(getResidentialHierarchyService.execute(prisonId, includeInactive, filters)).thenReturn(Response(data = listOf(mainLocation)))

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsJson<DataResponse<List<ResidentialHierarchyItem>>>().shouldBe(DataResponse(data = listOf(mainLocation)))
      }

      it("should call the audit service") {
        whenever(getResidentialHierarchyService.execute(prisonId, includeInactive, filters)).thenReturn(Response(data = listOf(mainLocation)))

        mockMvc.performAuthorised(path)
        verify(
          auditService,
          times(1),
        ).createEvent(
          "GET_PRISON_RESIDENTIAL_HIERARCHY",
          mapOf("prisonId" to prisonId),
        )
      }

      it("returns 400 when getResidentialHierarchyService returns not found") {
        whenever(getResidentialHierarchyService.execute(prisonId, includeInactive, filters)).thenReturn(
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

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(400)
      }

      it("returns 404 when getResidentialHierarchyService returns not found") {
        whenever(getResidentialHierarchyService.execute(prisonId, includeInactive, filters)).thenReturn(
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

    describe("GET /{prisonId}/residential-details") {
      val prisonId = "ABC"
      val parentPathHierarchy = "A"
      val filters = null
      val path = "$basePath/$prisonId/residential-details?parentPathHierarchy=$parentPathHierarchy"

      val residentialDetails =
        ResidentialDetails(
          topLevelLocationType = "Wings",
          subLocationName = "Wings",
          parentLocation =
            Location(
              key = "MKI-A",
              code = "A",
              pathHierarchy = "A",
              locationType = "WING",
              localName = "MKI-A",
              comments = null,
              capacity =
                LocationCapacity(
                  maxCapacity = 240,
                  workingCapacity = 1,
                ),
              certification =
                LocationCertification(
                  certified = true,
                  capacityOfCertifiedCell = 240,
                ),
              accommodationTypes = listOf("NORMAL_ACCOMMODATION"),
              specialistCellTypes = listOf("ACCESSIBLE_CELL", "CONSTANT_SUPERVISION"),
              usedFor = listOf("STANDARD_ACCOMMODATION"),
              status = "ACTIVE",
              convertedCellType = null,
              otherConvertedCellType = null,
              active = true,
              deactivatedByParent = false,
              level = 1,
              inactiveCells = 221,
              numberOfCellLocations = 222,
              isResidential = true,
              oldWorkingCapacity = null,
              usage = null,
              deactivatedDate = null,
              deactivatedReason = null,
              deactivationReasonDescription = null,
              deactivatedBy = null,
              proposedReactivationDate = null,
              planetFmReference = null,
              parentLocationKey = null,
            ),
          subLocations = listOf(),
        )

      beforeEach {
        Mockito.reset(getResidentialHierarchyService)
      }

      it("should return 200 when success") {
        whenever(getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)).thenReturn(Response(data = residentialDetails))

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsJson<DataResponse<ResidentialDetails>>().shouldBe(DataResponse(data = residentialDetails))
      }

      it("should call the audit service") {
        whenever(getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)).thenReturn(Response(data = residentialDetails))

        mockMvc.performAuthorised(path)
        verify(
          auditService,
          times(1),
        ).createEvent(
          "GET_PRISON_RESIDENTIAL_DETAILS",
          mapOf("prisonId" to prisonId, "parentPathHierarchy" to parentPathHierarchy),
        )
      }

      it("returns 400 when getResidentialDetailsService returns not found") {
        whenever(getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)).thenReturn(
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

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(400)
      }

      it("returns 404 when getResidentialDetailsService returns not found") {
        whenever(getResidentialDetailsService.execute(prisonId, parentPathHierarchy, filters)).thenReturn(
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

    describe("GET /{prisonId}/capacity") {
      val prisonId = "ABC"
      val filters = null
      val path = "$basePath/$prisonId/capacity"

      val prisonCapacity =
        LIPPrisonSummary(
          prisonName = "Example Prison",
          workingCapacity = 800,
          signedOperationalCapacity = 1000,
          maxCapacity = 1200,
          numberOfCellLocations = 300,
        ).toPrisonCapacity()

      beforeEach {
        Mockito.reset(getResidentialHierarchyService)
      }

      it("should return 200 when success") {
        whenever(getCapacityForPrisonService.execute(prisonId, filters)).thenReturn(Response(data = prisonCapacity))

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(HttpStatus.OK.value())
        result.response.contentAsJson<DataResponse<PrisonCapacity>>().shouldBe(DataResponse(data = prisonCapacity))
      }

      it("should call the audit service") {
        whenever(getCapacityForPrisonService.execute(prisonId, filters)).thenReturn(Response(data = prisonCapacity))

        mockMvc.performAuthorised(path)
        verify(
          auditService,
          times(1),
        ).createEvent(
          "GET_CAPACITY_DETAILS",
          mapOf("prisonId" to prisonId),
        )
      }

      it("returns 400 when getCapacityForPrisonService returns not found") {
        whenever(getCapacityForPrisonService.execute(prisonId, filters)).thenReturn(
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

        val result = mockMvc.performAuthorised(path)
        result.response.status.shouldBe(400)
      }

      it("returns 404 when getCapacityForPrisonService returns not found") {
        whenever(getCapacityForPrisonService.execute(prisonId, filters)).thenReturn(
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

    describe("GET /{prisonId}/location/{key}") {
      val prisonId = "MDI"
      val key = "A-1-001"
      val filters = null
      val path = "$basePath/$prisonId/location/$key"

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

      beforeEach {
        Mockito.reset(getResidentialHierarchyService)
      }

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
        val invalidPath = "$basePath/$prisonId/location/$invalidParam"
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

    describe("POST /{prisonId}/location/{key}/deactivate") {
      val prisonId = "MDI"
      val key = "A-1-001"
      val path = "/v1/prison/$prisonId/location/$key/deactivate"
      val deactivateLocationRequest =
        DeactivateLocationRequest(
          reason = DeactivationReason.DAMAGED,
          reasonDescription = "Scheduled maintenance",
          proposedReactivationDate = LocalDateTime.now(),
        )

      it("returns 200 when location is successfully deactivated") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, null)).thenReturn(
          Response(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(200)
        result.response.contentAsJson<DataResponse<HmppsMessageResponse>>().shouldBe(
          DataResponse(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )
      }

      it("returns 404 when location is not found") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, null)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "Location not found", causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON))),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(404)
      }

      it("returns 400 when invalid request data is provided") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, null)).thenReturn(
          Response(data = null, errors = listOf(UpstreamApiError(type = UpstreamApiError.Type.BAD_REQUEST, description = "Invalid data", causedBy = UpstreamApi.LOCATIONS_INSIDE_PRISON))),
        )

        val result = mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        result.response.status.shouldBe(400)
      }

      it("logs audit event when location is deactivated") {
        whenever(locationQueueService.sendDeactivateLocationRequest(deactivateLocationRequest, prisonId, key, null)).thenReturn(
          Response(data = HmppsMessageResponse(message = "Deactivate location written to queue")),
        )

        mockMvc.performAuthorisedPost(path, deactivateLocationRequest)
        verify(auditService, times(1)).createEvent(
          "DEACTIVATE_LOCATION",
          mapOf(
            "reason" to deactivateLocationRequest.reason.toString(),
            "reasonDescription" to deactivateLocationRequest.reasonDescription,
            "proposedReactivationDate" to deactivateLocationRequest.proposedReactivationDate.toString(),
          ),
        )
      }
    }
  })
