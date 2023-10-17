package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class RisksSmokeTest : DescribeSpec(
  {
    val pncId = "2004/13116M"
    val encodedPncId = URLEncoder.encode(pncId, StandardCharsets.UTF_8)

    val baseUrl = "http://localhost:8080"
    val basePath = "v1/persons/$encodedPncId/risks"

    val httpClient = HttpClient.newBuilder().build()
    val httpRequest = HttpRequest.newBuilder()

    it("returns risk scores for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath/scores")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
              {
                "completedDate": "2023-10-04T07:06:43",
                "assessmentStatus": "string",
                "generalPredictor": {
                    "scoreLevel": "LOW"
                },
                "violencePredictor": {
                    "scoreLevel": "LOW"
                },
                "groupReconviction": {
                    "scoreLevel": "LOW"
                },
                "riskOfSeriousRecidivism": {
                    "scoreLevel": "LOW"
                },
                "sexualPredictor": {
                    "indecentScoreLevel": "LOW",
                    "contactScoreLevel": "LOW"
                }
              }
          ],
          "pagination": {
              "isLastPage": true,
              "count": 1,
              "page": 1,
              "perPage": 10,
              "totalCount": 1,
              "totalPages": 1
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns rosh risks for a person") {
      val response = httpClient.send(
        httpRequest.uri(URI.create("$baseUrl/$basePath")).build(),
        HttpResponse.BodyHandlers.ofString(),
      )

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "assessedOn": "2023-10-04T07:06:43",
            "riskToSelf": {
              "suicide": {
                "risk": "YES",
                "previous": "YES",
                "previousConcernsText": "Risk of self harms concerns due to ...",
                "current": "YES",
                "currentConcernsText": "Risk of self harms concerns due to ..."
              },
              "selfHarm": {
                "risk": "YES",
                "previous": "YES",
                "previousConcernsText": "Risk of self harms concerns due to ...",
                "current": "YES",
                "currentConcernsText": "Risk of self harms concerns due to ..."
              },
              "custody": {
                "risk": "YES",
                "previous": "YES",
                "previousConcernsText": "Risk of self harms concerns due to ...",
                "current": "YES",
                "currentConcernsText": "Risk of self harms concerns due to ..."
              },
              "hostelSetting": {
                "risk": "YES",
                "previous": "YES",
                "previousConcernsText": "Risk of self harms concerns due to ...",
                "current": "YES",
                "currentConcernsText": "Risk of self harms concerns due to ..."
              },
              "vulnerability": {
                "risk": "YES",
                "previous": "YES",
                "previousConcernsText": "Risk of self harms concerns due to ...",
                "current": "YES",
                "currentConcernsText": "Risk of self harms concerns due to ..."
              }
            },
            "otherRisks": {
              "escapeOrAbscond": "YES",
              "controlIssuesDisruptiveBehaviour": "YES",
              "breachOfTrust": "YES",
              "riskToOtherPrisoners": "YES"
            },
            "summary": {
              "whoIsAtRisk": "X, Y and Z are at risk",
              "natureOfRisk": "The nature of the risk is X",
              "riskImminence": "the risk is imminent and more probably in X situation",
              "riskIncreaseFactors": "If offender in situation X the risk can be higher",
              "riskMitigationFactors": "Giving offender therapy in X will reduce the risk",
              "overallRiskLevel": "VERY_HIGH",
              "riskInCommunity": {
                "children": "HIGH ",
                "public": "HIGH ",
                "knowAdult": "HIGH ",
                "staff": "MEDIUM",
                "prisoners": "LOW"
              },
              "riskInCustody": {
                "knowAdult": "HIGH ",
                "staff": "VERY_HIGH",
                "prisoners": "VERY_HIGH",
                "children": "LOW",
                "public": "LOW"
              }
            }
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
