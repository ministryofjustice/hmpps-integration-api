package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
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
      val path = "/v1/persons/$hmppsId/alerts"
      val pndPath = "/v1/pnd/persons/$hmppsId/alerts"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getAlertsForPersonService)
          Mockito.reset(auditService)
          whenever(getAlertsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                listOf(
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
                  ),
                ),
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

          verify(getAlertsForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
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

        it("returns an empty list embedded in a JSON object when no alerts are found") {
          val hmppsIdForPersonWithNoAlerts = "B5678BB"
          val alertPath = "/v1/persons/$hmppsIdForPersonWithNoAlerts/alerts"

          whenever(getAlertsForPersonService.execute(hmppsIdForPersonWithNoAlerts, filters)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised(alertPath)

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = emptyList(),
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
          whenever(getAlertsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = emptyList(),
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

        it("returns paginated results") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                List(20) {
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
                },
            ),
          )

          val result = mockMvc.performAuthorised("$path?page=1&perPage=10")

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getAlertsForPersonService.execute(hmppsId, filters)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised("$path?page=1&perPage=10")

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
          whenever(getAlertsForPersonService.getAlertsForPnd(hmppsId)).thenReturn(
            Response(
              data =
                listOf(
                  Alert(
                    offenderNo = "A1111BB",
                    type = "B",
                    typeDescription = "Security again",
                    code = "BBB",
                    codeDescription = "For Release",
                    comment = "IS83",
                    dateCreated = LocalDate.parse("2022-09-01"),
                    dateExpired = LocalDate.parse("2023-09-01"),
                    expired = false,
                    active = false,
                  ),
                ),
            ),
          )
        }

        it("returns a 200 OK status code for PND") {
          val result = mockMvc.performAuthorised(pndPath)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("returns paginated results for PND") {
          whenever(getAlertsForPersonService.getAlertsForPnd(hmppsId)).thenReturn(
            Response(
              data =
                List(20) {
                  Alert(
                    offenderNo = "B0000ZZ",
                    type = "Z",
                    typeDescription = "Threat",
                    code = "BBB",
                    codeDescription = "Not For Release",
                    comment = "IS91",
                    dateCreated = LocalDate.parse("2022-09-01"),
                    dateExpired = LocalDate.parse("2023-10-01"),
                    expired = false,
                    active = false,
                  )
                },
            ),
          )

          val result = mockMvc.performAuthorised("$pndPath?page=1&perPage=10")

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
        }

        it("returns an empty list embedded in a JSON object when no alerts are found for PND") {
          val hmppsIdForPersonWithNoAlerts = "B5679BB"
          val alertPath = "/v1/pnd/persons/$hmppsIdForPersonWithNoAlerts/alerts"

          whenever(getAlertsForPersonService.getAlertsForPnd(hmppsIdForPersonWithNoAlerts)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised(alertPath)

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
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

          verify(getAlertsForPersonService, VerificationModeFactory.times(1)).getAlertsForPnd(hmppsId)
        }
      }
    },
  )
