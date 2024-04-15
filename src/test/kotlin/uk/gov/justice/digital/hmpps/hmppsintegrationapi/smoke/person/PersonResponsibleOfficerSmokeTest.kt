package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PersonResponsibleOfficerSmokeTest : DescribeSpec(
  {
    val hmppsId = "A1234AA"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val basePath = "v1/persons/$encodedHmppsId/person-responsible-officer"
    val httpClient = IntegrationAPIHttpClient()

    it("returns the person responsible officer for a person") {
      val response = httpClient.performAuthorised(basePath)
      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        "data": {
            "prisonOffenderManager": {
                "forename": "Paul",
                "surname": "Reds",
                "prison": {
                    "code": "PrisonCode1"
                }
            },
            "communityOffenderManager": {
                "name": {
                    "forename": "Helen",
                    "surname": "Miller"
                },
                "email": "helenemail@email.com",
                "telephoneNumber": "0987654321",
                "team": {
                    "code": "PrisonCode2",
                    "description": "Description",
                    "email": "email_again@email.com",
                    "telephoneNumber": "01234567890"
                }
            }
        }
        """.trimIndent(),
      )
    }
  },
)
