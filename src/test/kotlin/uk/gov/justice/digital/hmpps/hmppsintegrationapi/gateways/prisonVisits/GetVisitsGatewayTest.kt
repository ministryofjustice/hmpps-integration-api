package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonVisits

import io.kotest.core.spec.style.DescribeSpec
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

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonVisitsGateway::class],
)
class GetVisitsGatewayTest(
  @MockitoBean
  val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonVisitsGateway: PrisonVisitsGateway,
) : DescribeSpec(
    {
      val path = "/visits/search"
      val prisonId = "ABC"
      val hmppsId = "A1234AA"
      val fromDate = "2024-01-01"
      val toDate = "2024-01-14"
      val visitStatus = "BOOKED"
      val page = 1
      val size = 10
      val pathWithQueryParams = "$path?prisonId=$prisonId&visitStatus=$visitStatus&page=$page&size=$size&prisonerId=$hmppsId&visitStartDate=$fromDate&visitEndDate=$toDate"
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
        prisonVisitsGateway.getVisits(prisonId, hmppsId, fromDate, toDate, visitStatus, page, size)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("MANAGE-PRISON-VISITS")
      }

      it("returns a 200 when visit is found") {
        val exampleData =
          """
         {
   "totalElements":1,
   "totalPages":1,
   "first":true,
   "last":true,
   "size":1,
   "number":1,
   "sort":{
      "empty":false,
      "sorted":false,
      "unsorted":true
   },
   "numberOfElements":1,
   "pageable":{
      "offset":0,
      "sort":{
         "empty":false,
         "sorted":false,
         "unsorted":true
      },
      "pageSize":1,
      "paged":true,
      "pageNumber":1,
      "unpaged":false
   },
   "empty":false,
   "content":[
      {
         "applicationReference":"dfs-wjs-eqr",
         "reference":"dfs-wjs-abc",
         "prisonerId":"AF34567G",
         "prisonId":"MDI",
         "prisonName":"Moorland (HMP & YOI)",
         "sessionTemplateReference":"v9d.7ed.7u",
         "visitRoom":"Visits Main Hall",
         "visitType":"SOCIAL",
         "visitStatus":"RESERVED",
         "outcomeStatus":"VISITOR_CANCELLED",
         "visitRestriction":"OPEN",
         "startTimestamp":"2018-12-01T13:45:00",
         "endTimestamp":"2018-12-01T13:45:00",
         "visitNotes":[
            {
               "type":"VISITOR_CONCERN",
               "text":"Visitor is concerned that his mother in-law is coming!"
            }
         ],
         "visitContact":{
            "name":"John Smith",
            "telephone":"01234 567890",
            "email":"email@example.com"
         },
         "visitors":[
            {
               "nomisPersonId":1234,
               "visitContact":true
            }
         ],
         "visitorSupport":{
            "description":"visually impaired assistance"
         },
         "createdTimestamp":"2018-12-01T13:45:00",
         "modifiedTimestamp":"2018-12-01T13:45:00",
         "firstBookedDateTime":"2018-12-01T13:45:00"
      }
   ]
}
          """.trimIndent()

        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(pathWithQueryParams, body = exampleData, HttpStatus.OK)
        val response = prisonVisitsGateway.getVisits(prisonId, hmppsId, fromDate, toDate, visitStatus, page, size)
        response.data.shouldNotBeNull()
        response.data!!
          .visits
          .first()
          ?.visitStatus
          .shouldBe("RESERVED")
      }

      it("returns a 200 when multiple visits are found") {
        val exampleData =
          """
                   {
             "totalElements":1,
             "totalPages":1,
             "first":true,
             "last":true,
             "size":1,
             "number":1,
             "sort":{
                "empty":false,
                "sorted":false,
                "unsorted":true
             },
             "numberOfElements":1,
             "pageable":{
                "offset":0,
                "sort":{
                   "empty":false,
                   "sorted":false,
                   "unsorted":true
                },
                "pageSize":1,
                "paged":true,
                "pageNumber":1,
                "unpaged":false
             },
             "empty":false,
             "content":[
                {
                   "applicationReference":"dfs-wjs-eqr",
                   "reference":"dfs-wjs-abc",
                   "prisonerId":"AF34567G",
                   "prisonId":"MDI",
                   "prisonName":"Moorland (HMP & YOI)",
                   "sessionTemplateReference":"v9d.7ed.7u",
                   "visitRoom":"Visits Main Hall",
                   "visitType":"SOCIAL",
                   "visitStatus":"RESERVED",
                   "outcomeStatus":"VISITOR_CANCELLED",
                   "visitRestriction":"OPEN",
                   "startTimestamp":"2018-12-01T13:45:00",
                   "endTimestamp":"2018-12-01T13:45:00",
                   "visitNotes":[
                      {
                         "type":"VISITOR_CONCERN",
                         "text":"Visitor is concerned that his mother in-law is coming!"
                      }
                   ],
                   "visitContact":{
                      "name":"John Smith",
                      "telephone":"01234 567890",
                      "email":"email@example.com"
                   },
                   "visitors":[
                      {
                         "nomisPersonId":1234,
                         "visitContact":true
                      }
                   ],
                   "visitorSupport":{
                      "description":"visually impaired assistance"
                   },
                   "createdTimestamp":"2018-12-01T13:45:00",
                   "modifiedTimestamp":"2018-12-01T13:45:00",
                   "firstBookedDateTime":"2018-12-01T13:45:00"
                },
                {
                   "applicationReference":"abc-wjs-eqr",
                   "reference":"abc-wjs-abc",
                   "prisonerId":"AB1234G",
                   "prisonId":"MDI",
                   "prisonName":"Moorland (HMP & YOI)",
                   "sessionTemplateReference":"v9d.7ed.7u",
                   "visitRoom":"Visits Main Hall",
                   "visitType":"SOCIAL",
                   "visitStatus":"BOOKED",
                   "outcomeStatus":"VISITOR_CANCELLED",
                   "visitRestriction":"OPEN",
                   "startTimestamp":"2018-12-01T13:45:00",
                   "endTimestamp":"2018-12-01T13:45:00",
                   "visitNotes":[
                      {
                         "type":"VISITOR_CONCERN",
                         "text":"Visitor is concerned that his mother in-law is coming!"
                      }
                   ],
                   "visitContact":{
                      "name":"John Smith",
                      "telephone":"01234 567890",
                      "email":"email@example.com"
                   },
                   "visitors":[
                      {
                         "nomisPersonId":1234,
                         "visitContact":true
                      }
                   ],
                   "visitorSupport":{
                      "description":"visually impaired assistance"
                   },
                   "createdTimestamp":"2018-12-01T13:45:00",
                   "modifiedTimestamp":"2018-12-01T13:45:00",
                   "firstBookedDateTime":"2018-12-01T13:45:00"
                }
             ]
          }
          """.trimIndent()

        prisonVisitsApiMockServer.stubPrisonVisitsApiResponse(pathWithQueryParams, body = exampleData, HttpStatus.OK)

        val response = prisonVisitsGateway.getVisits(prisonId, hmppsId, fromDate, toDate, visitStatus, page, size)
        response.data.shouldNotBeNull()
        response.data!!
          .visits
          .first()
          ?.visitStatus
          .shouldBe("RESERVED")
        response.data!!
          .visits[1]
          ?.visitStatus
          .shouldBe("BOOKED")
      }
    },
  )
