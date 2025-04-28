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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPrisonersService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetVisitsService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [PrisonController::class])
@ActiveProfiles("test")
internal class PrisonControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPrisonersService: GetPrisonersService,
  @MockitoBean val getVisitsService: GetVisitsService,
  @Autowired val request: HttpServletRequest,
) : DescribeSpec(
    {
      val hmppsId = "200313116M"
      val basePath = "/v1/prison"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      afterTest {
        Mockito.reset(getPersonService)
        Mockito.reset(auditService)
        Mockito.reset(getPrisonersService)
      }

      describe("GET prisoners/$hmppsId") {
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

      describe("GET visit/search") {
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
    },
  )
