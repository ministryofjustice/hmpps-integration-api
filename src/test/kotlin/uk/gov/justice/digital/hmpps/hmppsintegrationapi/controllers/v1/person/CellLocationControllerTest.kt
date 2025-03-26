package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CellLocation
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCellLocationForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService

@WebMvcTest(controllers = [CellLocationController::class])
@ActiveProfiles("test")
internal class CellLocationControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getCellLocationForPersonService: GetCellLocationForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/cell-location"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val filters = null

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getCellLocationForPersonService)
          Mockito.reset(auditService)
          whenever(getCellLocationForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data =
                CellLocation(
                  cell = "6-2-006",
                  prisonCode = "MDI",
                  prisonName = "Moorland (HMP & YOI)",
                ),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the cell location for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getCellLocationForPersonService, VerificationModeFactory.times(1)).execute(hmppsId, filters)
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_CELL_LOCATION", mapOf("hmppsId" to hmppsId))
        }

        it("returns the cell location for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": {
               "prisonCode": "MDI",
               "prisonName": "Moorland (HMP & YOI)",
               "cell": "6-2-006"
            }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns null embedded in a JSON object when no cell location is found") {
          val hmppsIdForPersonNotInPrison = "A1234AA"
          val needsPath = "/v1/persons/$hmppsIdForPersonNotInPrison/cell-location"

          whenever(getCellLocationForPersonService.execute(hmppsIdForPersonNotInPrison, filters)).thenReturn(Response(data = null))

          val result = mockMvc.performAuthorised(needsPath)

          result.response.contentAsString.shouldContain("\"data\":null")
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getCellLocationForPersonService.execute(hmppsId, filters)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISONER_OFFENDER_SEARCH,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 400 BAD Request status code when an invalid hmpps id is found in the upstream API") {
          whenever(getCellLocationForPersonService.execute(hmppsId, filters)).thenReturn(
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
          whenever(getCellLocationForPersonService.execute(hmppsId, filters)).doThrow(
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
    },
  )
