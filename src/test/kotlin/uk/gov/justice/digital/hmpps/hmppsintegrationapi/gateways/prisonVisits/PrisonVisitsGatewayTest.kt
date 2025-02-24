package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonVisits

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonVisitsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.PrisonVisitsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonVisitsGateway::class],
)
class PrisonVisitsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec(
    {
      val visitReference = "123456"
      val path = "/visits/$visitReference"

      val prisonerId = "AF34567G"
      val futuresPath = "/visits/search/future/$prisonerId"

      val prisonVisitsApiMockServer = PrisonVisitsApiMockServer()

      beforeEach {
        prisonVisitsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)

        whenever(hmppsAuthGateway.getClientToken("MANAGE-PRISON-VISITS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        prisonVisitsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials for linked prisoners api") {
        prisonVisitsGateway.getVisitByReference(visitReference)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("MANAGE-PRISON-VISITS")
      }

      it("returns a 500 when request is incorrect") {
        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(path, body = "", HttpStatus.INTERNAL_SERVER_ERROR)

        val response = prisonVisitsGateway.getVisitByReference(visitReference)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.MANAGE_PRISON_VISITS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.INTERNAL_SERVER_ERROR)
      }

      it("returns a 404 when visit is not found") {
        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(path, body = "", HttpStatus.NOT_FOUND)

        val response = prisonVisitsGateway.getVisitByReference(visitReference)
        response.errors.shouldHaveSize(1)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.MANAGE_PRISON_VISITS)
        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.ENTITY_NOT_FOUND)
      }

      it("returns a 200 when visit is found") {
        val exampleData =
          """
          {
            "prisonerId": "AF34567G",
            "prisonId": "MDI",
            "prisonName": "Moorland (HMP & YOI)",
            "sessionTemplateReference": "v9d.7ed.7u",
            "visitRoom": "Visits Main Hall",
            "visitType": "SOCIAL",
            "visitStatus": "RESERVED",
            "outcomeStatus": "VISITOR_CANCELLED",
            "visitRestriction": "OPEN",
            "startTimestamp": "2018-12-01T13:45:00",
            "endTimestamp": "2018-12-01T13:45:00",
            "visitNotes": [
            {
              "type": "VISITOR_CONCERN",
              "text": "Visitor is concerned that his mother in-law is coming!"
            }
            ],
            "visitContact": {
            "name": "John Smith",
            "telephone": "01234 567890",
            "email": "email@example.com"
          },
            "visitors": [
            {
              "nomisPersonId": 1234,
              "visitContact": true
            }
            ],
            "visitorSupport": {
            "description": "visually impaired assistance"
          },
            "createdTimestamp": "2018-12-01T13:45:00",
            "modifiedTimestamp": "2018-12-01T13:45:00",
            "firstBookedDateTime": "2018-12-01T13:45:00"
          }
          """.trimIndent()

        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(path, body = exampleData, HttpStatus.OK)

        val response = prisonVisitsGateway.getVisitByReference(visitReference)
        response.data.shouldNotBeNull()
        response.data?.prisonerId.shouldBe("AF34567G")
      }

      describe("future visits") {
        it("authenticates using HMPPS Auth with credentials for linked prisoners api") {
          prisonVisitsGateway.getFutureVisits(prisonerId)

          verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("MANAGE-PRISON-VISITS")
        }

        it("returns a 200 when a future visit is found") {
          val exampleData =
            """
            [
              {
                "applicationReference": "dfs-wjs-eqr",
                "reference": "v9-d7-ed-7u",
                "prisonerId": "AF34567G",
                "prisonId": "MDI",
                "prisonName": "Moorland (HMP & YOI)",
                "sessionTemplateReference": "v9d.7ed.7u",
                "visitRoom": "Visits Main Hall",
                "visitType": "SOCIAL",
                "visitStatus": "RESERVED",
                "outcomeStatus": "VISITOR_CANCELLED",
                "visitRestriction": "OPEN",
                "startTimestamp": "2018-12-01T13:45:00",
                "endTimestamp": "2018-12-01T13:45:00",
                "visitNotes": [
                  {
                    "type": "VISITOR_CONCERN",
                    "text": "Visitor is concerned that his mother in-law is coming!"
                  }
                ],
                "visitContact": {
                  "name": "John Smith",
                  "telephone": "01234 567890",
                  "email": "email@example.com"
                },
                "visitors": [
                  {
                    "nomisPersonId": 1234,
                    "visitContact": true
                  }
                ],
                "visitorSupport": {
                  "description": "visually impaired assistance"
                },
                "createdTimestamp": "2018-12-01T13:45:00",
                "modifiedTimestamp": "2018-12-01T13:45:00",
                "firstBookedDateTime": "2018-12-01T13:45:00"
              }
            ]
            """.trimIndent()

          prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(futuresPath, body = exampleData, HttpStatus.OK)

          val response = prisonVisitsGateway.getFutureVisits(prisonerId)
          response.data.shouldNotBeNull()
          response.data!!.size.shouldBe(1)
          response.data!!
            .first()
            .prisonerId
            .shouldBe(prisonerId)
        }

        it("returns an empty list when no future visits found") {
          prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(futuresPath, body = "[]", HttpStatus.OK)

          val response = prisonVisitsGateway.getFutureVisits(prisonerId)
          response.data.shouldNotBeNull()
          response.data.shouldBe(emptyList())
        }

        it("returns a 400 response when prisoner Id is invalid") {
          prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(futuresPath, body = "", HttpStatus.BAD_REQUEST)

          val response = prisonVisitsGateway.getFutureVisits(prisonerId)
          response.data.shouldBe(null)
          response.errors.shouldHaveSize(1)
          response.errors.first().shouldBe(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.BAD_REQUEST))
        }

        it("returns a 500 when request is incorrect") {
          prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(futuresPath, body = "", HttpStatus.INTERNAL_SERVER_ERROR)

          val response = prisonVisitsGateway.getFutureVisits(prisonerId)
          response.errors.shouldHaveSize(1)
          response.errors
            .first()
            .shouldBe(UpstreamApiError(causedBy = UpstreamApi.MANAGE_PRISON_VISITS, type = UpstreamApiError.Type.INTERNAL_SERVER_ERROR))
        }
      }
    },
  )
