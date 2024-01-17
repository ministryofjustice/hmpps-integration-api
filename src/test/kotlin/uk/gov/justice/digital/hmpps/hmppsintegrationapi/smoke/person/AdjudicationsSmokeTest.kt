package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.core.spec.style.DescribeSpec
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class AdjudicationsSmokeTest : DescribeSpec(
  {
    val hmppsId = "G2996UX"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/reported-adjudications"
    val httpClient = IntegrationAPIHttpClient()

    it("returns adjudications for a person") {
      val response = httpClient.performAuthorised(basePath)

//      response.statusCode().shouldBe(HttpStatus.OK.value())
//      response.body().shouldEqualJson(
//        """
//         {
//          "data": [ {
//              "incidentDetails": {
//                "dateTimeOfIncident": "2021-07-05T10:35:17"
//              }
//            }
//          ]
//        }
//        """.removeWhitespaceAndNewlines(),
//      )
    }
  },
)
