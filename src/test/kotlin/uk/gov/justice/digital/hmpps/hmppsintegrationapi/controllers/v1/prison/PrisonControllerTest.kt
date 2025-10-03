package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.prison

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Location
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.LocationCertification
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonCapacity
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonPayBand
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PrisonRegime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ResidentialHierarchyItem
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.locationsInsidePrison.LIPPrisonSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.personas.personInProbationAndNomisPersona
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCapacityForPrisonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonPayBandsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonRegimeService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetResidentialDetailsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetResidentialHierarchyService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.RedactionService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.DayOfWeek
import java.time.LocalDate

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
  @MockitoBean val getResidentialDetailsService: GetResidentialDetailsService,
  @MockitoBean val getCapacityForPrisonService: GetCapacityForPrisonService,
  @MockitoBean val getPrisonRegimeService: GetPrisonRegimeService,
  @MockitoBean val getPrisonPayBandsService: GetPrisonPayBandsService,
  @MockitoBean val redactionService: RedactionService,
) : DescribeSpec(
    {
      val firstName = personInProbationAndNomisPersona.firstName
      val lastName = personInProbationAndNomisPersona.lastName
      val dateOfBirth = personInProbationAndNomisPersona.dateOfBirth
      val hmppsId = personInProbationAndNomisPersona.identifiers.nomisNumber!!
      val basePath = "/v1/prison"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      beforeTest {
        doAnswer { invocation ->
          invocation.arguments[0]
        }.whenever(redactionService).applyPolicies(any(), any(), any())
      }

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
                  firstName = firstName,
                  lastName = lastName,
                  middleName = "Jonas",
                  dateOfBirth = dateOfBirth,
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
            "data": {
              "firstName": "$firstName",
              "lastName": "$lastName",
              "middleName": "Jonas",
              "dateOfBirth": "$dateOfBirth",
              "gender": "Male",
              "ethnicity": "Caucasian",
              "aliases": [],
              "identifiers": {
                "nomisNumber": null,
                "croNumber": null,
                "deliusCrn": null
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
                  firstName = firstName,
                  lastName = lastName,
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

        val path = "$basePath/prisoners?first_name=$firstName&last_name=$lastName&date_of_birth=$dateOfBirth"

        it("returns 500 when prison/prisoners throws an unexpected error") {
          whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, null)).thenThrow(RuntimeException("Service error"))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(500)
        }

        it("returns 500 when prisoner query gets no result") {
          whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, ConsumerFilters(emptyList()))).thenReturn(
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
          whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, null)).thenReturn(
            Response(
              data =
                listOf(
                  PersonInPrison(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = "Jonas",
                    dateOfBirth = dateOfBirth,
                    youthOffender = false,
                  ),
                  PersonInPrison(
                    firstName = firstName,
                    lastName = lastName,
                    middleName = "Rock",
                    dateOfBirth = dateOfBirth,
                    youthOffender = false,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("$path&search_within_aliases=false")
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("returns 403 when consumer config includes an empty prison filter field") {
          whenever(getPrisonersService.execute(firstName, lastName, dateOfBirth.toString(), false, ConsumerFilters(prisons = emptyList()))).thenReturn(
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
            visitSubStatus = "AUTO_APPROVED",
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

        it("returns 400 when getResidentialHierarchyService returns Bad Request") {
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
                externalReference = null,
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

        it("returns 400 when getResidentialDetailsService Bad Request") {
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

        it("returns 400 when getCapacityForPrisonService returns bad request") {
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

      describe("GET /{prisonId}/prison-regime") {
        val prisonId = "ABC"
        val filters = null
        val path = "$basePath/$prisonId/prison-regime"

        val prisonRegime =
          PrisonRegime(
            amStart = "09:00",
            amFinish = "12:00",
            pmStart = "13:00",
            pmFinish = "17:00",
            edStart = "18:00",
            edFinish = "21:00",
            dayOfWeek = DayOfWeek.MONDAY,
          )

        beforeEach {
          Mockito.reset(getPrisonRegimeService)
        }

        it("should return 200 when success") {
          whenever(getPrisonRegimeService.execute(prisonId, filters)).thenReturn(Response(data = listOf(prisonRegime)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<DataResponse<List<PrisonRegime>>>().shouldBe(DataResponse(data = listOf(prisonRegime)))
        }

        it("should call the audit service") {
          whenever(getPrisonRegimeService.execute(prisonId, filters)).thenReturn(Response(data = listOf(prisonRegime)))

          mockMvc.performAuthorised(path)
          verify(
            auditService,
            times(1),
          ).createEvent(
            "GET_PRISON_REGIME",
            mapOf("prisonId" to prisonId),
          )
        }

        it("returns 400 when getPrisonRegimeService returns bad request") {
          whenever(getPrisonRegimeService.execute(prisonId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.BAD_REQUEST,
                    causedBy = UpstreamApi.ACTIVITIES,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when getPrisonRegimeService returns not found") {
          whenever(getPrisonRegimeService.execute(prisonId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    causedBy = UpstreamApi.ACTIVITIES,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(404)
        }
      }

      describe("GET /{prisonId}/prison-pay-bands") {
        val prisonId = "ABC"
        val filters = null
        val path = "$basePath/$prisonId/prison-pay-bands"

        val prisonPayBand =
          PrisonPayBand(
            id = 123456,
            alias = "Low",
            description = "Pay band 1",
          )

        beforeEach {
          Mockito.reset(getPrisonPayBandsService)
        }

        it("should return 200 when success") {
          whenever(getPrisonPayBandsService.execute(prisonId, filters)).thenReturn(Response(data = listOf(prisonPayBand)))

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
          result.response.contentAsJson<DataResponse<List<PrisonPayBand>>>().shouldBe(DataResponse(data = listOf(prisonPayBand)))
        }

        it("should call the audit service") {
          whenever(getPrisonPayBandsService.execute(prisonId, filters)).thenReturn(Response(data = listOf(prisonPayBand)))

          mockMvc.performAuthorised(path)
          verify(
            auditService,
            times(1),
          ).createEvent(
            "GET_PRISON_PAY_BANDS",
            mapOf("prisonId" to prisonId),
          )
        }

        it("returns 400 when getPrisonPayBandsService returns bad request") {
          whenever(getPrisonPayBandsService.execute(prisonId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.BAD_REQUEST,
                    causedBy = UpstreamApi.ACTIVITIES,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(400)
        }

        it("returns 404 when getPrisonPayBandsService returns not found") {
          whenever(getPrisonPayBandsService.execute(prisonId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    causedBy = UpstreamApi.ACTIVITIES,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(404)
        }
      }
    },
  )
