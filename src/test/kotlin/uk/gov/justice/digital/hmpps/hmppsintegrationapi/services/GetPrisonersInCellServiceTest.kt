package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerOffenderSearchGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchMatcher
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchQuery
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSAttributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPageable
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPaginatedPrisoners
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSPrisoner
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisoneroffendersearch.POSSort

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetPrisonersInCellService::class],
)
class GetPrisonersInCellServiceTest(
  private val getPrisonersInCellService: GetPrisonersInCellService,
  @MockitoBean val prisonerOffenderSearchGateway: PrisonerOffenderSearchGateway,
) : DescribeSpec({
    val prisonId = "MKI"
    val cellLocation = "A-1-001"
    val request =
      POSAttributeSearchRequest(
        joinType = "AND",
        queries =
          listOf(
            POSAttributeSearchQuery(
              joinType = "AND",
              matchers =
                listOf(
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "prisonId",
                    condition = "IS",
                    searchTerm = prisonId,
                  ),
                  POSAttributeSearchMatcher(
                    type = "String",
                    attribute = "cellLocation",
                    condition = "IS",
                    searchTerm = cellLocation,
                  ),
                ),
            ),
          ),
      )
    val posPrisoners =
      listOf(
        POSPrisoner(
          firstName = "Joe",
          lastName = "Blogs",
          prisonId = prisonId,
          cellLocation = cellLocation,
          youthOffender = false,
        ),
      )

    beforeEach {
      whenever(prisonerOffenderSearchGateway.attributeSearch(request)).thenReturn(
        Response(
          data =
            POSPaginatedPrisoners(
              content = posPrisoners,
              totalElements = 1,
              totalPages = 1,
              first = true,
              last = true,
              size = 10,
              number = 0,
              sort =
                POSSort(
                  empty = false,
                  sorted = false,
                  unsorted = false,
                ),
              numberOfElements = 1,
              pageable =
                POSPageable(
                  offset = 0,
                  sort =
                    POSSort(
                      empty = false,
                      sorted = false,
                      unsorted = false,
                    ),
                  pageSize = 10,
                  pageNumber = 1,
                  paged = true,
                  unpaged = false,
                ),
              empty = false,
            ),
        ),
      )
    }

    it("should return a list of prisoners in a cell") {
      val result = getPrisonersInCellService.execute(prisonId, cellLocation)
      result.data.shouldNotBeNull()
      result.data.shouldBe(posPrisoners.map { it.toPersonInPrison() })
    }

    it("should return an errors if prisonerOffenderSearchGateway returns an error") {
      val errors = listOf(UpstreamApiError(UpstreamApi.PRISONER_OFFENDER_SEARCH, UpstreamApiError.Type.BAD_REQUEST, description = "Bad Request from prisonerOffenderSearchGateway"))
      whenever(prisonerOffenderSearchGateway.attributeSearch(request)).thenReturn(
        Response(
          data = null,
          errors,
        ),
      )

      val result = getPrisonersInCellService.execute(prisonId, cellLocation)
      result.data.shouldBeNull()
      result.errors.shouldBe(errors)
    }
  })
