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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVPaginatedVisits
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PVVistExternalSystemDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitsService::class],
)
internal class GetVisitsServiceTest(
  private val getVisitsService: GetVisitsService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
  @MockitoBean val getPersonService: GetPersonService,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val prisonId = "ABC"
    val filters = ConsumerFilters(null)
    val visitResponse =
      PVVisit(
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
        visitContact = PVVisitContact(name = "Name", telephone = "Telephone", email = "Email"),
        applicationReference = "dfs-wjs-abc",
        reference = "dfs-wjs-abc",
        sessionTemplateReference = "dfs-wjs-xyz",
        visitorSupport = PVVisitorSupport(description = "Description"),
        visitExternalSystemDetails =
          PVVistExternalSystemDetails(
            clientName = "client_name",
            clientVisitReference = "12345",
          ),
      )
    val paginatedVisitsData =
      PVPaginatedVisits(
        content = listOf(visitResponse),
        totalCount = 1L,
        isLastPage = true,
        count = 1,
        page = 1,
        perPage = 1,
        totalPages = 1,
      )

    beforeEach {
      Mockito.reset(prisonVisitsGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PaginatedVisits>(prisonId, filters, upstreamServiceType = UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(
        Response(data = null),
      )
      whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(
        Response(data = NomisNumber(hmppsId)),
      )
    }

    it("will return 200 and a list of visits") {
      whenever(prisonVisitsGateway.getVisits(prisonId, hmppsId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10)).thenReturn(
        Response(data = paginatedVisitsData),
      )
      val response = getVisitsService.execute(hmppsId, prisonId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10, filters)
      response.data.shouldNotBeNull()
    }

    it("will return 404 when prison filter not in consumer profile") {
      val wrongPrisonId = "LCK"
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PVPaginatedVisits>(wrongPrisonId, filters, upstreamServiceType = UpstreamApi.MANAGE_PRISON_VISITS)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
      )
      val response = getVisitsService.execute(hmppsId, wrongPrisonId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10, filters)
      response.data.shouldBe(null)
      response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("will return errors from failed get noms number request") {
      whenever(getPersonService.getNomisNumber(hmppsId)).thenReturn(
        Response(data = null, errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found"))),
      )
      val response = getVisitsService.execute(hmppsId, prisonId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10, filters)
      response.data.shouldBe(null)
      response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.ENTITY_NOT_FOUND, "Not found")))
    }

    it("will return 400 bad request when visit status is invalid") {
      val incorrectStatus = "SCHEMING"
      val response = getVisitsService.execute(hmppsId, prisonId, "2021-01-01", "2021-01-02", incorrectStatus, 1, 10, filters)
      response.data.shouldBe(null)
      response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.MANAGE_PRISON_VISITS, UpstreamApiError.Type.BAD_REQUEST, "Invalid visit status")))
    }
  })
