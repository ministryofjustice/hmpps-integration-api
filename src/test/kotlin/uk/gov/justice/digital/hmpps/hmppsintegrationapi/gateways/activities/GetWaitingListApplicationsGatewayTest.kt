package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.activities

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ActivitiesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesEarliestReleaseDate
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesPagedWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesSort
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListApplication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.activities.ActivitiesWaitingListSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [ActivitiesGateway::class],
)
class GetWaitingListApplicationsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val activitiesGateway: ActivitiesGateway,
) : DescribeSpec(
    {
      val mockServer = ApiMockServer.create(UpstreamApi.ACTIVITIES)
      val prisonCode = "MDI"
      val activitiesWaitingListSearchRequest =
        ActivitiesWaitingListSearchRequest(
          applicationDateFrom = LocalDate.now(),
          applicationDateTo = LocalDate.now().plusDays(1),
          activityId = 123456L,
          prisonerNumbers = listOf("A1234AA"),
          status = listOf("DECLINED", "PENDING"),
        )
      val objectMapper = jacksonObjectMapper()
      val jsonRequest = objectMapper.writeValueAsString(activitiesWaitingListSearchRequest.toApiConformingMap())
      val page = 1
      val pageSize = 50

      beforeEach {
        mockServer.start()

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ACTIVITIES")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterEach {
        mockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        activitiesGateway.getWaitingListApplications(prisonCode, activitiesWaitingListSearchRequest, page, pageSize)

        verify(hmppsAuthGateway, times(1)).getClientToken("ACTIVITIES")
      }

      it("Returns an activity schedule") {
        mockServer.stubForPost(
          "/integration-api/waiting-list-applications/$prisonCode/search?page=${page - 1}&pageSize=$pageSize",
          reqBody = jsonRequest,
          File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/activities/fixtures/GetWaitingListApplications.json").readText(),
        )

        val result = activitiesGateway.getWaitingListApplications(prisonCode, activitiesWaitingListSearchRequest, page, pageSize)
        result.errors.shouldBeEmpty()
        result.data.shouldNotBeNull()
        result.data.shouldBe(
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
                  prisonCode = "MDI",
                  prisonerNumber = "G2996UX",
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
          ),
        )
      }

      it("Returns a bad request error") {
        mockServer.stubForPost(
          "/integration-api/waiting-list-applications/$prisonCode/search?page=${page - 1}&pageSize=$pageSize",
          reqBody = jsonRequest,
          status = HttpStatus.BAD_REQUEST,
          resBody = "{}",
        )

        val result = activitiesGateway.getWaitingListApplications(prisonCode, activitiesWaitingListSearchRequest, page, pageSize)
        result.errors.shouldBe(listOf(UpstreamApiError(causedBy = UpstreamApi.ACTIVITIES, type = UpstreamApiError.Type.BAD_REQUEST)))
      }
    },
  )
