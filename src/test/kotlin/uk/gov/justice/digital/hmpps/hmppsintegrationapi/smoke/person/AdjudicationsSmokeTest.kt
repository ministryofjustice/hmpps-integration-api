package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
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

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
       {
        "data": [
        {
          "incidentDetails": {
            "locationId": -9007199254740991,
            "dateTimeOfIncident": "2021-07-05T10:35:17",
            "dateTimeOfDiscovery": "2021-07-05T10:35:17",
            "handoverDeadline": "2021-07-05T10:35:17"
          },
          "isYouthOffender": true,
          "incidentRole": {
            "roleCode": "25a",
            "offenceRule": {
              "paragraphNumber": "25(a)",
              "paragraphDescription": "Committed an assault"
            },
            "associatedPrisonersNumber": "G2996UX",
            "associatedPrisonersName": "G2996UX"
          },
          "offenceDetails": {
            "offenceCode": 3,
            "offenceRule": {
              "paragraphNumber": "25(a)",
              "paragraphDescription": "Committed an assault",
              "nomisCode": "string",
              "withOthersNomisCode": "string"
            },
            "victimPrisonersNumber": "G2996UX",
            "victimStaffUsername": "ABC12D",
            "victimOtherPersonsName": "Bob Hope"
          },
          "hearings": [
            {
              "id": -9007199254740991,
              "locationId": -9007199254740991,
              "dateTimeOfHearing": "2021-07-05T10:35:17",
              "oicHearingType": "GOV_ADULT",
              "outcome": {
                "id": -9007199254740991,
                "adjudicator": "string",
                "code": "COMPLETE",
                "reason": "LEGAL_ADVICE",
                "details": "string",
                "plea": "UNFIT"
              },
              "agencyId": "string"
            }
          ],
          "outcomes": [
            {
              "hearing": {
                "id": -9007199254740991,
                "locationId": -9007199254740991,
                "dateTimeOfHearing": "2021-07-05T10:35:17",
                "oicHearingType": "GOV_ADULT",
                "outcome": {
                  "id": -9007199254740991,
                  "adjudicator": "string",
                  "code": "COMPLETE",
                  "reason": "LEGAL_ADVICE",
                  "details": "string",
                  "plea": "UNFIT"
                },
                "agencyId": "string"
              },
              "outcome": {
                "outcome": {
                  "id": -9007199254740991,
                  "code": "REFER_POLICE",
                  "details": "string",
                  "reason": "ANOTHER_WAY",
                  "quashedReason": "FLAWED_CASE",
                  "canRemove": true
                },
                "referralOutcome": {
                  "id": -9007199254740991,
                  "code": "REFER_POLICE",
                  "details": "string",
                  "reason": "ANOTHER_WAY",
                  "quashedReason": "FLAWED_CASE",
                  "canRemove": true
                }
              }
            }
          ],
          "punishments": [
            {
              "id": -9007199254740991,
              "type": "PRIVILEGE",
              "privilegeType": "CANTEEN",
              "otherPrivilege": "string",
              "stoppagePercentage": -2147483648,
              "activatedBy": "string",
              "activatedFrom": "string",
              "schedule": {
                "days": -2147483648,
                "startDate": "2019-08-24",
                "endDate": "2019-08-24",
                "suspendedUntil": "2019-08-24"
              },
              "consecutiveChargeNumber": "string",
              "consecutiveReportAvailable": true,
              "damagesOwedAmount": -1.7976931348623157E308,
              "canRemove": true
            }
          ],
          "punishmentComments": [
            {
              "id": -9007199254740991,
              "comment": "string",
              "reasonForChange": "APPEAL",
              "createdByUserId": "string",
              "dateTime": "2021-07-05T10:35:17"
            }
          ]
        }
      ],
      "pagination": {
        "isLastPage": true,
        "count": 1,
        "page": 1,
        "perPage": 8,
        "totalCount": 1,
        "totalPages": 1
      }
    }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
