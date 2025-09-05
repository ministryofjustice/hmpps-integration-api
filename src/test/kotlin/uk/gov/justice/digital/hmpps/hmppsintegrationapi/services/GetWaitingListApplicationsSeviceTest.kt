package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.ConsumerPrisonAccessService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesEarliestReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPagedWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedWaitingListApplications
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.WaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleFilters
import java.time.LocalDate
import java.time.LocalDateTime

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetWaitingListApplicationsService::class],
)
internal class GetWaitingListApplicationsSeviceTest(
  @MockitoBean val activitiesGateway: ActivitiesGateway,
  @MockitoBean val consumerPrisonAccessService: ConsumerPrisonAccessService,
  private val getWaitingListApplicationsService: GetWaitingListApplicationsService,
) : DescribeSpec({
    val prisonId = "ABC"
    val filters = RoleFilters(null)
    val waitingListSearchRequest =
      WaitingListSearchRequest(
        applicationDateFrom = LocalDate.now(),
        applicationDateTo = LocalDate.now().plusDays(1),
      )
    val page = 1
    val perPage = 50
    val exampleGatewayResponse =
      ActivitiesPagedWaitingListApplication(
        totalPages = 0,
        totalElements = 0,
        first = true,
        last = true,
        size = 0,
        content =
          listOf(
            ActivitiesWaitingListApplication(
              id = 111111L,
              activityId = 1000,
              scheduleId = 222222,
              allocationId = 333333,
              prisonCode = "PVI",
              prisonerNumber = "A1234AA",
              bookingId = 10001,
              status = "PENDING",
              statusUpdatedTime = LocalDateTime.parse("2023-06-04T16:30:00"),
              requestedDate = LocalDate.parse("2023-06-23"),
              requestedBy = "Fred Bloggs",
              comments = "The prisoner has specifically requested to attend this activity",
              declinedReason = "The prisoner has specifically requested to attend this activity",
              creationTime = LocalDateTime.parse("2023-01-03T12:00:00"),
              createdBy = "Jon Doe",
              updatedTime = LocalDateTime.parse("2023-01-04T16:30:00"),
              updatedBy = "Jane Doe",
              earliestReleaseDate =
                ActivitiesEarliestReleaseDate(
                  releaseDate = "2027-09-20",
                  isTariffDate = true,
                  isIndeterminateSentence = true,
                  isImmigrationDetainee = true,
                  isConvictedUnsentenced = true,
                  isRemand = true,
                ),
              nonAssociations = true,
            ),
          ),
        number = 0,
        sort =
          ActivitiesSort(
            empty = true,
            sorted = true,
            unsorted = true,
          ),
        numberOfElements = 0,
        pageable =
          ActivitiesPageable(
            offset = 0,
            sort =
              ActivitiesSort(
                empty = true,
                sorted = true,
                unsorted = true,
              ),
            pageSize = 0,
            paged = true,
            pageNumber = 0,
            unpaged = true,
          ),
        empty = true,
      )

    beforeEach {
      Mockito.reset(consumerPrisonAccessService, activitiesGateway)

      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<PaginatedWaitingListApplications>(prisonId, filters, UpstreamApi.ACTIVITIES)).thenReturn(
        Response(data = null),
      )

      whenever(
        activitiesGateway.getWaitingListApplications(
          prisonId,
          waitingListSearchRequest.toActivitiesWaitingListSearchRequest(),
          page,
          perPage,
        ),
      ).thenReturn(
        Response(
          data = exampleGatewayResponse,
        ),
      )
    }

    it("gets data from the gateway") {
      val result =
        getWaitingListApplicationsService.execute(
          prisonId,
          waitingListSearchRequest,
          filters,
          page,
          perPage,
        )

      verify(activitiesGateway, times(1)).getWaitingListApplications(
        prisonId,
        waitingListSearchRequest.toActivitiesWaitingListSearchRequest(),
      )

      result.data.shouldBe(exampleGatewayResponse.toPaginatedWaitingListApplications())
    }

    it("records upstream API errors") {
      whenever(consumerPrisonAccessService.checkConsumerHasPrisonAccess<List<PaginatedWaitingListApplications>>(prisonId, filters, UpstreamApi.ACTIVITIES)).thenReturn(
        Response(
          data = null,
          errors =
            listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ACTIVITIES,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
        ),
      )

      val response =
        getWaitingListApplicationsService.execute(
          prisonId,
          waitingListSearchRequest,
          filters,
          page,
          perPage,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.ACTIVITIES,
          type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
        ).shouldBe(true)
    }

    it("records upstream API errors gateway returns error") {
      whenever(
        activitiesGateway.getWaitingListApplications(
          prisonId,
          waitingListSearchRequest.toActivitiesWaitingListSearchRequest(),
          page,
          perPage,
        ),
      ).thenReturn(
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
      val response =
        getWaitingListApplicationsService.execute(
          prisonId,
          waitingListSearchRequest,
          filters,
          page,
          perPage,
        )
      response
        .hasErrorCausedBy(
          causedBy = UpstreamApi.ACTIVITIES,
          type = UpstreamApiError.Type.BAD_REQUEST,
        ).shouldBe(true)
    }
  })
