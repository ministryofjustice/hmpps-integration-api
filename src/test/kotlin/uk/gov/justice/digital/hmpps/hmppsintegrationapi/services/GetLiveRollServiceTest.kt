package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPaginatedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSSort

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLiveRollService::class],
)
internal class GetLiveRollServiceTest(
  private val getLiveRollService: GetLiveRollService,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) : DescribeSpec({
    val prisonId = "ABC"
    val requestContext = buildRequestContext("testUser")
    val paginatedLiveRoll =
      POSPaginatedPrisoners(
        content =
          listOf(
            POSPrisoner(
              firstName = "firstName",
              lastName = "lastName",
              youthOffender = false,
            ),
            POSPrisoner(
              firstName = "firstName",
              lastName = "lastName",
              youthOffender = false,
            ),
          ),
        totalPages = 1,
        totalElements = 2,
        first = true,
        last = true,
        size = 2,
        number = 2,
        sort =
          POSSort(
            empty = false,
            sorted = true,
            unsorted = false,
          ),
        numberOfElements = 2,
        pageable =
          POSPageable(
            offset = 1,
            sort =
              POSSort(
                empty = false,
                sorted = true,
                unsorted = false,
              ),
            pageSize = 10,
            pageNumber = 1,
            paged = true,
            unpaged = false,
          ),
        empty = false,
      )

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
    }

    it("will return 200 and a list of prisoners") {
      whenever(
        prisonerOffenderSearchGateway.getPersonsFromPrisonId(
          prisonId,
          page = 1,
          size = 10,
          requestContext = requestContext,
        ),
      ).thenReturn(
        Response(data = paginatedLiveRoll),
      )
      val response =
        getLiveRollService.execute(
          prisonId,
          page = 1,
          size = 10,
          requestContext = requestContext,
        )
      response.data.shouldNotBeNull()
    }

    it("will return 404 with no prisoners") {
      whenever(
        prisonerOffenderSearchGateway.getPersonsFromPrisonId(
          "123",
          page = 1,
          size = 10,
          requestContext = requestContext,
        ),
      ).thenReturn(
        Response(
          data =
            POSPaginatedPrisoners(
              content = emptyList(),
              totalPages = 1,
              totalElements = 0,
              first = true,
              last = true,
              size = 0,
              number = 0,
              sort =
                POSSort(
                  empty = false,
                  sorted = true,
                  unsorted = false,
                ),
              numberOfElements = 0,
              pageable =
                POSPageable(
                  offset = 1,
                  sort =
                    POSSort(
                      empty = true,
                      sorted = true,
                      unsorted = false,
                    ),
                  pageSize = 10,
                  pageNumber = 1,
                  paged = true,
                  unpaged = false,
                ),
              empty = true,
            ),
        ),
      )
      try {
        getLiveRollService.execute(
          "123",
          page = 1,
          size = 10,
          requestContext = requestContext,
        )
      } catch (e: Exception) {
        e.shouldNotBeNull()
        e.message.shouldBe("Prison ID 123 did not return any persons")
      }
    }
  })
