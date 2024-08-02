package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class RisksSmokeTest : DescribeSpec(
  {
    val hmppsId = "A1234AA"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)

    val basePath = "v1/persons/$encodedHmppsId/risks"
    val httpClient = IntegrationAPIHttpClient()

    it("returns risk scores for a person") {
      val response = httpClient.performAuthorised("$basePath/scores")

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

    it("returns risk categories for a person") {
      val response = httpClient.performAuthorised("$basePath/categories")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
        "data": {
          "offenderNo": "A1234AA",
          "assessments": [
            {
              "classificationCode": "C",
              "classification": "Cat C",
              "assessmentCode": "CATEGORY",
              "assessmentDescription": "Categorisation",
              "assessmentDate": "2018-02-11",
              "nextReviewDate": "2018-02-11",
              "assessmentAgencyId": "MDI",
              "assessmentStatus": "P",
              "assessmentComment": "Comment details"
            }
          ],
          "category": "string",
          "categoryCode": "string"
        }
      }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns mappa detail for a person") {
      val response = httpClient.performAuthorised("$basePath/mappadetail")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": {
            "level": -2147483648,
            "levelDescription": "string",
            "category": -2147483648,
            "categoryDescription": "string",
            "startDate": "2019-08-24",
            "reviewDate": "2019-08-24",
            "notes": "string"
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns dynamic risks for a person") {
      val response = httpClient.performAuthorised("$basePath/dynamic")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
            {
              "code": "RCCO",
              "description": "Child Concerns",
              "startDate": "2019-08-24",
              "reviewDate": "2019-08-24",
              "notes": "string"
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
      val response = httpClient.performAuthorised("$basePath/serious-harm")

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
