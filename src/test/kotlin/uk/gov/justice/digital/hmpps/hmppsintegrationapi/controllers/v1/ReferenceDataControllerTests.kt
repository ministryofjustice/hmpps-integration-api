package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1

import ReferenceData
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.common.CrnSupplier
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.ReferenceDataService

@WebMvcTest(controllers = [ReferenceDataController::class])
@ActiveProfiles("test")
internal class ReferenceDataControllerTests(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val referenceDataService: ReferenceDataService,
  @Autowired val objectMapper: ObjectMapper,
  @MockitoBean val crnSupplier: CrnSupplier,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      lateinit var referenceData: Response<ReferenceData?>
      beforeTest {
        Mockito.reset(referenceDataService)
        referenceData = Response(objectMapper.readValue(testData(), ReferenceData::class.java))
        whenever(referenceDataService.referenceData()).thenReturn(referenceData)
      }

      describe("returns reference data") {
        val mockMvc = IntegrationAPIMockMvc(springMockMvc)
        val path = "/v1/hmpps/reference-data"

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)
          val resObj = result.response.contentAsJson<Response<ReferenceData?>>()
          result.response.status.shouldBe(HttpStatus.OK.value())
          resObj.shouldBe(referenceData)
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
