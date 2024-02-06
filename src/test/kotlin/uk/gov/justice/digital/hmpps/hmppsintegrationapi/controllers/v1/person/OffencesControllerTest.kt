package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.*
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetOffencesForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [OffencesController::class])
@ActiveProfiles("test")
internal class OffencesControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockBean val getOffencesForPersonService: GetOffencesForPersonService,
  @MockBean val auditService: AuditService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/offences"
    val mockMvc = IntegrationAPIMockMvc(springMockMvc)

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getOffencesForPersonService)
        Mockito.reset(auditService)
        whenever(getOffencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = listOf(
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                cjsCode = "RR99999",
                hoCode = "05800",
                courtDates = listOf(LocalDate.parse("2023-03-03")),
                description = "This is a description of an offence.",
                endDate = LocalDate.parse("2023-02-01"),
                startDate = LocalDate.parse("2023-01-01"),
                statuteCode = "RR99",
              ),
            ),
          ),
        )
      }

      it("returns a 200 OK status code") {
        val result = mockMvc.performAuthorised(path)

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("gets the offences for a person with the matching ID") {
        mockMvc.performAuthorised(path)

        verify(getOffencesForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the offences for a person with the matching ID") {
        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain(
          """
          "data": [
            {
              "serviceSource": "NDELIUS",
              "systemSource": "PROBATION_SYSTEMS",
              "cjsCode": "RR99999",
              "hoCode": "05800",
              "courtDates": ["2023-03-03"],
              "description": "This is a description of an offence.",
              "endDate":"2023-02-01",
              "startDate": "2023-01-01",
              "statuteCode":"RR99"
            }
          ]
        """.removeWhitespaceAndNewlines(),
        )
      }
      it("logs audit") {
        mockMvc.performAuthorised(path)

        verify(auditService, VerificationModeFactory.times(1)).createEvent("GET_PERSON_OFFENCES", "Person offences details with hmpps id: $hmppsId has been retrieved")
      }

      it("returns an empty list embedded in a JSON object when no offences are found") {
        val hmppsIdForPersonWithNoOffences = "0000/11111A"
        val encodedHmppsIdForPersonWithNoOffences =
          URLEncoder.encode(hmppsIdForPersonWithNoOffences, StandardCharsets.UTF_8)
        val path = "/v1/persons/$encodedHmppsIdForPersonWithNoOffences/offences"

        whenever(getOffencesForPersonService.execute(hmppsIdForPersonWithNoOffences)).thenReturn(
          Response(
            data = emptyList(),
          ),
        )

        val result = mockMvc.performAuthorised(path)

        result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
      }

      it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
        whenever(getOffencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = emptyList(),
            errors = listOf(
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

      it("returns paginated results") {
        whenever(getOffencesForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data =
            List(20) {
              Offence(
                serviceSource = UpstreamApi.NDELIUS,
                systemSource = SystemSource.PROBATION_SYSTEMS,
                cjsCode = "RR99999",
                courtDates = listOf(LocalDate.parse("2023-03-03")),
                description = "This is a description of an offence.",
                endDate = LocalDate.parse("2023-02-01"),
                startDate = LocalDate.parse("2023-01-01"),
                statuteCode = "RR99",
              )
            },
          ),
        )

        val result = mockMvc.performAuthorised("$path?page=1&perPage=10")

        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
        result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
      }
    }
  },
)
