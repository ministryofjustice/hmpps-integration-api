package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.prisonerAlerts

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.PrisonerAlertsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.prisonerAlerts.PAPaginatedAlerts

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [PrisonerAlertsGateway::class],
)
class GetAlertsForPrisonerTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  private val prisonerAlertsGateway: PrisonerAlertsGateway,
) : DescribeSpec(
    {
      val apiMockServer = ApiMockServer.create(UpstreamApi.PRISONER_ALERTS)
      val prisonerNumber = "zyx987"
      val page = 1
      val size = 10
      val path = "/prisoners/$prisonerNumber/alerts?page=${page - 1}&size=$size"

      fun responseJson(uuid: String) =
        """
          {
            "totalElements": 9007199254740991,
            "totalPages": 1073741824,
            "first": true,
            "last": true,
            "size": 1073741824,
            "content": [
              {
                "alertUuid": "$uuid",
                "prisonNumber": "A1234AA",
                "alertCode": {
                  "alertTypeCode": "A",
                  "alertTypeDescription": "Alert type description",
                  "code": "ABC",
                  "description": "Alert code description"
                },
                "description": "Alert description",
                "authorisedBy": "A. Nurse, An Agency",
                "activeFrom": "2021-09-27",
                "activeTo": "2022-07-15",
                "isActive": true,
                "createdAt": "2021-09-27T14:19:25",
                "createdBy": "USER1234",
                "createdByDisplayName": "Firstname Lastname",
                "lastModifiedAt": "2022-07-15T15:24:56",
                "lastModifiedBy": "USER1234",
                "lastModifiedByDisplayName": "Firstname Lastname",
                "activeToLastSetAt": "2022-07-15T15:24:56",
                "activeToLastSetBy": "USER1234",
                "activeToLastSetByDisplayName": "Firstname Lastname",
                "prisonCodeWhenCreated": "LEI"
              }
            ],
            "number": 1073741824,
            "sort": {
              "empty": true,
              "sorted": true,
              "unsorted": true
            },
            "numberOfElements": 1073741824,
            "pageable": {
              "offset": 9007199254740991,
              "sort": {
                "empty": true,
                "sorted": true,
                "unsorted": true
              },
              "unpaged": true,
              "pageSize": 1073741824,
              "paged": true,
              "pageNumber": 1073741824
            },
            "empty": true
          }
          """.removeWhitespaceAndNewlines()

      fun generateStubForAlertCodeParams() {
        val uuid = "9ff5babb-b859-4d2c-b42f-4d054df19e91"
        val pathWithCodes = "$path&alertCode=${PAPaginatedAlerts.PND_ALERT_CODES.joinToString(",")}"
        apiMockServer.stubForGet(
          pathWithCodes,
          responseJson(uuid),
        )
      }

      beforeEach {
        apiMockServer.start()
        apiMockServer.stubForGet(
          path,
          responseJson("8cdadcf3-b003-4116-9956-c99bd8df6a00"),
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("PRISONER_ALERTS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        apiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        prisonerAlertsGateway.getPrisonerAlertsForCodes(prisonerNumber, page, size)

        verify(hmppsAuthGateway, times(1)).getClientToken("PRISONER_ALERTS")
      }

      it("returns alerts for the matching person ID") {
        val response = prisonerAlertsGateway.getPrisonerAlertsForCodes(prisonerNumber, page, size)
        response.data.shouldNotBeNull()
        response.data!!
          .content.size
          .shouldBeGreaterThan(0)
        response.data!!
          .content
          .first()
          .alertUuid
          .shouldBe("8cdadcf3-b003-4116-9956-c99bd8df6a00")
      }

      it("returns an error when 404 Not Found is returned because no person is found") {
        apiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = prisonerAlertsGateway.getPrisonerAlertsForCodes(prisonerNumber, page, size)
        response.hasErrorCausedBy(UpstreamApiError.Type.ENTITY_NOT_FOUND, UpstreamApi.PRISONER_ALERTS)
      }

      it("should request for alerts with codes in query params when the code list is present") {
        // Create a stub with the expected path with a distinct uuid
        generateStubForAlertCodeParams()
        val response = prisonerAlertsGateway.getPrisonerAlertsForCodes(prisonerNumber, page, size, PAPaginatedAlerts.PND_ALERT_CODES)
        // If the path has resolved to a stub and returned the json with the uuid, the request has used the path without codes
        response.data.shouldNotBeNull()
        response.data!!
          .content
          .first()
          .alertUuid
          .shouldBe("9ff5babb-b859-4d2c-b42f-4d054df19e91")
      }

      it("should request for alerts without codes in query params when the code list is empty") {
        // Create a stub with the expected path with a distinct uuid
        generateStubForAlertCodeParams()
        val response = prisonerAlertsGateway.getPrisonerAlertsForCodes(prisonerNumber, page, size, emptyList())
        // If the path has NOT resolved to a stub and returned the json with the uuid, the request has used the path without alert codes
        response.data.shouldNotBeNull()
        response.data!!
          .content
          .first()
          .alertUuid
          .shouldBe("8cdadcf3-b003-4116-9956-c99bd8df6a00")
      }
    },
  )
