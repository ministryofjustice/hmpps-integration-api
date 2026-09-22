package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext.Companion.buildRequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedLiveRoll
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PersonInPrison
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetLiveRollService::class],
)
internal class GetLiveRollServiceTest(
  private val getLiveRollService: GetLiveRollService,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) : DescribeSpec({
    val prisonId = "ABC"
    val filters = ConsumerFilters(null)
    val requestContext = buildRequestContext("testUser", filters = filters)
    val paginatedLiveRoll =
      PaginatedLiveRoll(
        content =
          listOf(
            PersonInPrison(
              firstName = "firstName",
              lastName = "lastName",
              middleName = "Jonas",
              youthOffender = false,
            ),
            PersonInPrison(
              firstName = "firstName",
              lastName = "lastName",
              middleName = "Rock",
              youthOffender = false,
            ),
          ),
        totalPages = 1,
        totalCount = 2,
        isLastPage = true,
        count = 2,
        page = 1,
        perPage = 10,
      )

    beforeEach {
      Mockito.reset(prisonerOffenderSearchGateway)
    }

    it("will return 200 and a list of prisoners") {
      whenever(
        getLiveRollService.execute(
          prisonId,
          page = 1,
          size = 10,
          term = null,
          alerts = null,
          fromDate = null,
          toDate = null,
          cellLocationPrefix = null,
          incentiveLevelCode = null,
          responseFields = null,
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
          term = null,
          alerts = null,
          fromDate = null,
          toDate = null,
          cellLocationPrefix = null,
          incentiveLevelCode = null,
          responseFields = null,
          requestContext = requestContext,
        )
      response.data.shouldNotBeNull()
    }
  })
