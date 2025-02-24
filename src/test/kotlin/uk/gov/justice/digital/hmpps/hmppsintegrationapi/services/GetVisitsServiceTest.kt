package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.TransactionCreateResponse
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.Pageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.PaginatedVisit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.Sort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.Visit
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitContact
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonVisits.VisitorSupport
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetVisitsService::class],
)
internal class GetVisitsServiceTest(
  private val getVisitsService: GetVisitsService,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  @MockitoBean val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val prisonId = "ABC"
    val filters = ConsumerFilters(null)
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
      )
    val paginatedVisitsData =
      PaginatedVisit(
        totalElements = 1,
        totalPages = 1,
        first = true,
        last = true,
        number = 1,
        size = 1,
        numberOfElements = 1,
        empty = false,
        pageable = Pageable(offset = 1, sort = Sort(empty = false, sorted = false, unsorted = true), pageSize = 1, paged = true, pageNumber = 1, unpaged = true),
        sort = Sort(empty = false, sorted = false, unsorted = true),
        visits = listOf(visitResponse),
      )

    beforeEach {
      Mockito.reset(prisonVisitsGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<TransactionCreateResponse>(prisonId, filters)).thenReturn(
        Response(data = null),
      )
    }

    it("will return 200 and a list of visits") {
      whenever(prisonVisitsGateway.getVisits(prisonId, hmppsId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10)).thenReturn(
        Response(data = paginatedVisitsData),
      )
      val response = getVisitsService.execute(hmppsId, prisonId, "2021-01-01", "2021-01-02", "BOOKED", 1, 10, filters)
      response.data.shouldNotBeNull()
    }

    // 200
    // 400
    // 404 prison filter
    //
  })
