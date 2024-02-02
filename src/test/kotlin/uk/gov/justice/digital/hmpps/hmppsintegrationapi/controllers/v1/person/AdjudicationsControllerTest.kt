package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Adjudication
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.IncidentDetailsDto
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetAdjudicationsForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [AdjudicationsController::class])
@ActiveProfiles("test")
internal class AdjudicationsControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getAdjudicationsForPersonService: GetAdjudicationsForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/reported-adjudications"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getAdjudicationsForPersonService)
        Mockito.reset(auditService)
        whenever(getAdjudicationsForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = listOf(
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
        whenever(getAdjudicationsForPersonService.execute(hmppsId = "notfound")).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
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

        verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_PERSON_ADJUDICATIONS", "Person adjudications details with hmpps id: $hmppsId has been retrieved")
      }

      it("returns paginated adjudication results") {
        whenever(getAdjudicationsForPersonService.execute(hmppsId)).thenReturn(
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
  },
)
