package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.incentives

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.IncentivesGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [IncentivesGateway::class],
)
class GetIEPReviewHistoryTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val incentivesGateway: IncentivesGateway,
) : DescribeSpec({
    val hmppsId = "A1234AA"
    val path = "/incentive-reviews/prisoner/$hmppsId"
    val incentivesApiMockServer = ApiMockServer.create(UpstreamApi.INCENTIVES)

    beforeEach {
      incentivesApiMockServer.start()
      Mockito.reset(hmppsAuthGateway)

      whenever(hmppsAuthGateway.getClientToken("INCENTIVES")).thenReturn(HmppsAuthMockServer.TOKEN)
    }

    afterTest {
      incentivesApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials for Incentives API") {
      incentivesGateway.getIEPReviewHistory(hmppsId)

      verify(hmppsAuthGateway, times(1)).getClientToken("INCENTIVES")
    }

    it("gets the data from the gateway") {
      incentivesApiMockServer.stubForGet(
        path = path,
        body =
          """
          {
            "id": 12345,
            "iepCode": "STD",
            "iepLevel": "Standard",
            "prisonerNumber": "A1234BC",
            "bookingId": 1234567,
            "iepDate": "2021-12-31",
            "iepTime": "2021-12-31T12:34:56.789012",
            "iepDetails": [
              {
                "id": 12345,
                "iepLevel": "Standard",
                "iepCode": "STD",
                "comments": "A review took place",
                "prisonerNumber": "A1234BC",
                "bookingId": 1234567,
                "iepDate": "2021-12-31",
                "iepTime": "2021-12-31T12:34:56.789012",
                "agencyId": "MDI",
                "userId": "USER_1_GEN",
                "reviewType": "REVIEW",
                "auditModuleName": "INCENTIVES_API",
                "isRealReview": true
              }
            ],
            "nextReviewDate": "2022-12-31",
            "daysSinceReview": 23
          }
          """.trimIndent(),
      )

      val response = incentivesGateway.getIEPReviewHistory(hmppsId)
      response.errors.shouldBeEmpty()
      response.data.shouldNotBeNull()
      response.data!!.id.shouldBe(12345)
      response.data!!.iepCode.shouldBe("STD")
      response.data!!.iepLevel shouldBe ("Standard")
    }

    it("should return error when bad request is returned") {
      incentivesApiMockServer.stubForGet(path, "", HttpStatus.BAD_REQUEST)
      val response = incentivesGateway.getIEPReviewHistory(hmppsId)
      response.errors.shouldBe(listOf(UpstreamApiError(UpstreamApi.INCENTIVES, UpstreamApiError.Type.BAD_REQUEST)))
    }
  })
