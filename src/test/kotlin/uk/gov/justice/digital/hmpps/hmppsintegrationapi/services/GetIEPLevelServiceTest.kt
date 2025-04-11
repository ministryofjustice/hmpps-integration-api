package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.IncentivesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.NomisNumber
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.incentives.IncIEPDetails
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.incentives.IncIEPReviewHistory
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.ConsumerFilters

@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [GetIEPLevelService::class],
)
internal class GetIEPLevelServiceTest(
  @MockitoBean val incentivesGateway: IncentivesGateway,
  @MockitoBean val getPersonService: GetPersonService,
  private val getIEPLevelService: GetIEPLevelService,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val filters = ConsumerFilters(null)

    val iepReviewHistory =
      IncIEPReviewHistory(
        id = 12345,
        iepCode = "STD",
        iepLevel = "Standard",
        prisonerNumber = hmppsId,
        bookingId = 12345L,
        iepDate = "2025-02-01",
        iepTime = "2021-12-31T12:34:56.789012",
        iepDetails =
          listOf(
            IncIEPDetails(
              id = 12345,
              iepCode = "STD",
              iepLevel = "Standard",
              prisonerNumber = hmppsId,
              bookingId = 12345L,
              iepDate = "2025-02-01",
              iepTime = "2021-12-31T12:34:56.789012",
              agencyId = "MDI",
              userId = "USER_1_GEN",
              reviewType = "REVIEW",
              auditModuleName = "INCENTIVES_API",
              isRealReview = true,
            ),
          ),
        nextReviewDate = "2025-02-01",
        daysSinceReview = 5,
      )

    beforeEach {
      Mockito.reset(incentivesGateway)
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = NomisNumber(hmppsId)))
    }

    it("returns IEP level review history") {
      whenever(incentivesGateway.getIEPReviewHistory(hmppsId)).thenReturn(Response(data = iepReviewHistory, errors = emptyList()))

      val result = getIEPLevelService.execute(hmppsId, filters)

      result.shouldNotBeNull()
      result.shouldBe(Response(data = iepReviewHistory.toIEPLevel()))
    }

    it("failed incentives gateway call") {
      val err = listOf(UpstreamApiError(UpstreamApi.INCENTIVES, UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
      whenever(incentivesGateway.getIEPReviewHistory(hmppsId)).thenReturn(Response(data = null, errors = err))
      val result = getIEPLevelService.execute(hmppsId, filters)
      result.errors.shouldBe(err)
    }

    it("failed prison check call") {
      val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND, description = "NOMIS number not found"))
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = null, errors = err))
      val result = getIEPLevelService.execute(hmppsId, filters)
      result.errors.shouldBe(err)
    }

    it("failed to get prisoners nomis number") {
      val err = listOf(UpstreamApiError(UpstreamApi.NOMIS, UpstreamApiError.Type.ENTITY_NOT_FOUND))
      whenever(getPersonService.getNomisNumberWithPrisonFilter(hmppsId, filters)).thenReturn(Response(data = null))
      val result = getIEPLevelService.execute(hmppsId, filters)
      result.errors.shouldBe(err)
    }
  })
