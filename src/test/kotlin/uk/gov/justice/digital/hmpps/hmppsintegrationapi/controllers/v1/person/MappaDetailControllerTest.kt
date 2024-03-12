package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.MappaDetail
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetMappaDetailForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@WebMvcTest(controllers = [MappaDetailController::class])
@ActiveProfiles("test")
internal class MappaDetailControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getMappaDetailForPersonService: GetMappaDetailForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/risks/mappadetail"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getMappaDetailForPersonService)
        whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
          Response(
            MappaDetail(
              level = 2,
              levelDescription = "A high level of risk",
              category = 3,
              categoryDescription = "Behaviour",
              startDate = "2024-03-08",
              reviewDate = "2024-10-08",
              notes = "Review in a week",
            ),
          ),
        )

        Mockito.reset(auditService)
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the mappa detail for a person with the matching ID") {
        mockMvc.performAuthorised(path)
        verify(getMappaDetailForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("logs audit") {
        mockMvc.performAuthorised(path)

        verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_MAPPA_DETAIL", "Mappa detail for person with hmpps id: $hmppsId has been retrieved")
      }

      it("returns the risk categories for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
          "data": {
              "level": 2,
              "levelDescription": "A high level of risk",
              "category": 3,
              "categoryDescription": "Behaviour",
              "startDate": "2024-03-08",
              "reviewDate": "2024-10-08",
              "notes": "Review in a week"
          }
        """.removeWhitespaceAndNewlines(),
        )
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getMappaDetailForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = MappaDetail(),
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.PROBATION_OFFENDER_SEARCH,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }
  },
)
