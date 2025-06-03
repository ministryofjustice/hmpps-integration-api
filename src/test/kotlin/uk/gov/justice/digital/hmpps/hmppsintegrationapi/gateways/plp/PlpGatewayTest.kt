package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.plp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
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
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PLPGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import java.io.File

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PLPGateway::class],
)
class PlpGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val plpGateway: PLPGateway,
) : DescribeSpec(
    {
      val nomsNumber = "X1234YZ"
      val path = "/inductions/$nomsNumber/induction-schedule"
      val plpMockServer = ApiMockServer.create(UpstreamApi.PLP)

      beforeEach {
        plpMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("PLP")).thenReturn(
          HmppsAuthMockServer.TOKEN,
        )
      }

      afterTest {
        plpMockServer.stop()
      }

      describe("getInductionSchedule") {
        it("authenticates using HMPPS Auth with credentials") {
          plpGateway.getInductionSchedule(nomsNumber)
          verify(hmppsAuthGateway, times(1)).getClientToken("PLP")
        }

        it("upstream API returns an error, throw exception") {
          plpMockServer.stubForGet(path, "", HttpStatus.BAD_REQUEST)

          val response =
            shouldThrow<WebClientResponseException> {
              plpGateway.getInductionSchedule(nomsNumber)
            }
          response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
        }

        it("returns induction schedule") {
          plpMockServer.stubForGet(
            path,
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetInductionScheduleResponse.json",
            ).readText(),
          )

          val response = plpGateway.getInductionSchedule(nomsNumber)
          response.data.shouldNotBeNull()
          response.data.status.shouldBe("SCHEDULED")
          response.data.nomisNumber.shouldBe(nomsNumber)
        }
      }

      describe("getInductionScheduleHistory") {
        it("authenticates using HMPPS Auth with credentials") {
          plpGateway.getInductionScheduleHistory(nomsNumber)
          verify(hmppsAuthGateway, times(1)).getClientToken("PLP")
        }

        it("upstream API returns an error, throw exception") {
          plpMockServer.stubForGet("$path/history", "", HttpStatus.BAD_REQUEST)

          val response =
            shouldThrow<WebClientResponseException> {
              plpGateway.getInductionScheduleHistory(nomsNumber)
            }
          response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
        }

        it("returns induction schedule history") {
          plpMockServer.stubForGet(
            "$path/history",
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetInductionScheduleHistoryResponse.json",
            ).readText(),
          )

          val response = plpGateway.getInductionScheduleHistory(nomsNumber)
          response.data.shouldNotBeNull()
          response.data.inductionSchedules[0]
            .status
            .shouldBe("SCHEDULED")
          response.data.inductionSchedules[0]
            .nomisNumber
            .shouldBe(nomsNumber)
        }
      }

      describe("getReviewSchedules") {
        it("authenticates using HMPPS Auth with credentials") {
          plpGateway.getReviewSchedules(nomsNumber)
          verify(hmppsAuthGateway, times(1)).getClientToken("PLP")
        }

        it("upstream API returns an error, throw exception") {
          plpMockServer.stubForGet("/action-plans/$nomsNumber/reviews/review-schedules", "", HttpStatus.BAD_REQUEST)

          val response =
            shouldThrow<WebClientResponseException> {
              plpGateway.getReviewSchedules(nomsNumber)
            }
          response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
        }

        it("returns review schedules") {
          plpMockServer.stubForGet(
            "/action-plans/$nomsNumber/reviews/review-schedules",
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetReviewSchedulesResponse.json",
            ).readText(),
          )

          val response = plpGateway.getReviewSchedules(nomsNumber)
          response.data.shouldNotBeNull()
          response.data.reviewSchedules[0]
            .status
            .shouldBe("SCHEDULED")
          response.data.reviewSchedules[0]
            .calculationRule
            .shouldBe("BETWEEN_12_AND_60_MONTHS_TO_SERVE")
        }
      }

      describe("getReviews") {
        it("authenticates using HMPPS Auth with credentials") {
          plpGateway.getReviews(nomsNumber)
          verify(hmppsAuthGateway, times(1)).getClientToken("PLP")
        }

        it("upstream API returns an error, throw exception") {
          plpMockServer.stubForGet("/action-plans/$nomsNumber/reviews", "", HttpStatus.BAD_REQUEST)

          val response =
            shouldThrow<WebClientResponseException> {
              plpGateway.getReviews(nomsNumber)
            }
          response.statusCode.shouldBe(HttpStatus.BAD_REQUEST)
        }

        it("returns reviews") {
          plpMockServer.stubForGet(
            "/action-plans/$nomsNumber/reviews",
            File(
              "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/plp/fixtures/GetReviewsResponse.json",
            ).readText(),
          )

          val response = plpGateway.getReviews(nomsNumber)
          response.data.latestReviewSchedule.shouldNotBeNull()
          response.data.completedReviews[0]
            .updatedBy
            .shouldBe("NRUSSELL_GEN")
        }
      }
    },
  )
