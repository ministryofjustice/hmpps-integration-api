package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Court
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.CourtType
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.ReferenceData
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetCourtService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReferenceDataService

@WebMvcTest(controllers = [ReferenceDataController::class])
@Import(WebMvcTestConfiguration::class)
@ActiveProfiles("test")
internal class ReferenceDataControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val referenceDataService: ReferenceDataService,
  @MockitoBean val getReferenceDataService: GetCourtService,
  @Autowired val objectMapper: ObjectMapper,
) : DescribeSpec(
    {
      lateinit var referenceData: Response<ReferenceData?>
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      beforeTest {
        Mockito.reset(referenceDataService)
        referenceData = Response(objectMapper.readValue(testData(), ReferenceData::class.java))
        whenever(referenceDataService.referenceData()).thenReturn(referenceData)
      }

      describe("returns reference data") {
        val path = "/v1/hmpps/reference-data"

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          val resObj = result.response.contentAsJson<Response<ReferenceData?>>()
          result.response.status.shouldBe(HttpStatus.OK.value())
          resObj.shouldBe(referenceData)
        }
      }
      describe("GET /v1/hmpps/reference-data/courts/{courtId}") {
        val courtId = "ACCRYC"
        val path = "/v1/hmpps/reference-data/courts/$courtId"

        beforeEach {
          Mockito.reset(getReferenceDataService)
          whenever(getReferenceDataService.getCourt(eq(courtId), any())).thenReturn(
            Response(
              Court(
                "ACCRYC",
                "Accrington Youth Court",
                "Accrington Youth Court",
                CourtType(
                  "COU",
                  "County Court/County Divorce Ct",
                ),
                true,
              ),
            ),
          )
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets prison timeline for a person with the matching ID") {
          mockMvc.performAuthorised(path)
          verify(getReferenceDataService, VerificationModeFactory.times(1)).getCourt(eq(courtId), any())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getReferenceDataService.getCourt(eq(courtId), any())).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.COURT_REGISTER,
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

fun testData(): String =
  """
  {
      "prisonReferenceData": {
        "PHONE_TYPE": [
          {
            "code": "a",
            "description": "desc_a"
          },
          {
            "code": "b",
            "description": "desc_b"
          },
          {
            "code": "c",
            "description": "desc_c"
          }
        ],
        "ALERT_TYPE": [
          {
            "code": "a",
            "description": "desc_a"
          },
          {
            "code": "b",
            "description": "desc_b"
          },
          {
            "code": "c",
            "description": "desc_c"
          }
        ],
        "ETHNICITY": [
          {
            "code": "a",
            "description": "desc_a"
          },
          {
            "code": "b",
            "description": "desc_b"
          },
          {
            "code": "c",
            "description": "desc_c"
          }
        ],
        "GENDER": [
          {
            "code": "a",
            "description": "desc_a"
          },
          {
            "code": "b",
            "description": "desc_b"
          },
          {
            "code": "c",
            "description": "desc_c"
          }
        ]
      },
      "probationReferenceData": {
        "GENDER": [
          {
            "code": "M",
            "description": "Male"
          },
          {
            "code": "F",
            "description": "Female"
          }
        ]
      }
  }
  """.trimIndent()
