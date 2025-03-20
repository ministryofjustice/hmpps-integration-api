package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Alert
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.PaginatedAlerts
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAlertsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.time.LocalDate

@WebMvcTest(controllers = [AlertsController::class])
@ActiveProfiles("test")
internal class AlertsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val getAlertsForPersonService: GetAlertsForPersonService,
  @MockitoBean val auditService: AuditService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val filters = null
      val page = 1
      val perPage = 10
      val path = "/v1/persons/$hmppsId/alerts"
      val pndPath = "/v1/pnd/persons/$hmppsId/alerts"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val alert =
        Alert(
          offenderNo = "A7777ZZ",
          type = "X",
          typeDescription = "Security",
          code = "XNR",
          codeDescription = "Not For Release",
          comment = "IS91",
          dateCreated = LocalDate.parse("2022-08-01"),
          dateExpired = LocalDate.parse("2023-08-01"),
          expired = false,
          active = true,
        )

      fun toPaginatedAlerts(alerts: List<Alert>) =
        PaginatedAlerts(
          content = alerts,
          count = alerts.size,
          page = page,
          perPage = perPage,
          isLastPage = true,
          totalCount = alerts.size.toLong(),
          totalPages = 1,
        )

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getAlertsForPersonService)
          Mockito.reset(auditService)

          whenever(getAlertsForPersonService.execute(hmppsId, filters, page, perPage)).thenReturn(
            Response(
              data = toPaginatedAlerts(listOf(alert)),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_ALERTS", mapOf("hmppsId" to hmppsId))
        }

        it("gets the alerts for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getAlertsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, filters, page, perPage)
        }

        it("returns the alerts for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": [
            {
              "offenderNo": "A7777ZZ",
              "type": "X",
              "typeDescription": "Security",
              "code": "XNR",
              "codeDescription": "Not For Release",
              "comment": "IS91",
              "dateCreated": "2022-08-01",
              "dateExpired": "2023-08-01",
              "expired": false,
              "active": true
            }
          ]
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters, page, perPage)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 Bad request status code when nomis id is invalid in the upstream API") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters, page, perPage)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.NOMIS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters, page, perPage)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised(path)
          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }

      describe("GET $pndPath") {
        beforeTest {
          Mockito.reset(getAlertsForPersonService)
          Mockito.reset(auditService)

          whenever(getAlertsForPersonService.execute(hmppsId, filters, page, perPage, pndOnly = true)).thenReturn(
            Response(
              data = toPaginatedAlerts(listOf(alert)),
            ),
          )
        }

        it("returns a 200 OK status code for PND") {
          val result = mockMvc.performAuthorised(pndPath)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit for PND") {
          mockMvc.performAuthorised(pndPath)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_ALERTS_PND", mapOf("hmppsId" to hmppsId))
        }

        it("gets the alerts for PND for a person with the matching ID") {
          mockMvc.performAuthorised(pndPath)

          verify(getAlertsForPersonService, times(1)).execute(hmppsId, filters, page, perPage, pndOnly = true)
        }
      }
    },
  )
