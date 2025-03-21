package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAdjudicationsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [AdjudicationsController::class])
@ActiveProfiles("test")
internal class AdjudicationsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getAdjudicationsForPersonService: GetAdjudicationsForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getPersonService: GetPersonService,
  @MockitoBean val crnSupplier: CrnSupplier,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/reported-adjudications"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getAdjudicationsForPersonService)
          Mockito.reset(auditService)
          Mockito.reset(getPersonService)
          whenever(getAdjudicationsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                listOf(
                  Adjudication(
                    incidentDetails =
                      IncidentDetailsDto(
                        dateTimeOfIncident = "2021-04-03T10:00:00",
                      ),
                  ),
                ),
            ),
          )
        }

        it("throws exception when no person found") {
          whenever(getAdjudicationsForPersonService.execute(hmppsId = "notfound", filters)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                    causedBy = UpstreamApi.ADJUDICATIONS,
                  ),
                ),
            ),
          )
          val noFoundPath = "/v1/persons/notfound/reported-adjudications"
          val result = mockMvc.performAuthorised(noFoundPath)
          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("logs audit for adjudications") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_ADJUDICATIONS", mapOf("hmppsId" to hmppsId))
        }

        it("returns paginated adjudication results") {
          whenever(getAdjudicationsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                List(20) {
                  Adjudication(
                    IncidentDetailsDto(dateTimeOfIncident = "2022-05-05T11:00:00"),
                  )
                },
            ),
          )

          val result = mockMvc.performAuthorised("$path?page=1&perPage=15")

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
        }
      }

      describe("GET $path with upstream service down") {
        beforeTest {
          Mockito.reset(getAdjudicationsForPersonService)
          Mockito.reset(auditService)
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getAdjudicationsForPersonService.execute(hmppsId, filters)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised("$path?page=1&perPage=15")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }

        it("returns a 400 Bad request status code when hmpps id invalid in the upstream API") {
          whenever(getAdjudicationsForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.ADJUDICATIONS,
                    type = UpstreamApiError.Type.BAD_REQUEST,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.BAD_REQUEST.value())
        }
      }
    },
  )
