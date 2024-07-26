package uk.gov.justice.digital.hmpps.hmppsintegrationapi.smoke.person

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIHttpClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class SentencesSmokeTest : DescribeSpec(
  {
    val hmppsId = "2004/13116M"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val basePath = "v1/persons/$encodedHmppsId/sentences"
    val httpClient = IntegrationAPIHttpClient()

    it("returns sentences for a person") {
      val response = httpClient.performAuthorised(basePath)

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
          "data": [
              {
                "serviceSource": "NOMIS",
                "systemSource": "PRISON_SYSTEMS",
                "dateOfSentencing": "2019-08-24",
                "description": "string",
                "isActive": null,
                "isCustodial": true,
                "fineAmount": -1.7976931348623157E308,
                "length": {
                  "duration": null,
                  "units": null,
                  "terms": [
                    {
                      "years": 1,
                      "months": 2,
                      "weeks": 3,
                      "days": 4,
                      "hours": null,
                      "prisonTermCode": "string"
                    }
                  ]
                }
              },
              {
                "serviceSource": "NDELIUS",
                "systemSource": "PROBATION_SYSTEMS",
                "dateOfSentencing": "2019-08-24",
                "description": "string",
                "isActive": true,
                "isCustodial": false,
                "fineAmount": null,
                "length": {
                  "duration": -2147483648,
                  "units": "Hours",
                  "terms": []
                }
              }
            ],
          "pagination": {
            "isLastPage": true,
            "count": 2,
            "page": 1,
            "perPage": 10,
            "totalCount": 2,
            "totalPages": 1
          }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }

    it("returns latest sentence key dates and adjustments for a person") {
      val response = httpClient.performAuthorised("$basePath/latest-key-dates-and-adjustments")

      response.statusCode().shouldBe(HttpStatus.OK.value())
      response.body().shouldEqualJson(
        """
        {
            "data": {
                "adjustments": {
                    "additionalDaysAwarded": 12,
                    "unlawfullyAtLarge": 12,
                    "lawfullyAtLarge": 12,
                    "restoredAdditionalDaysAwarded": 12,
                    "specialRemission": 12,
                    "recallSentenceRemand": 12,
                    "recallSentenceTaggedBail": 12,
                    "remand": 12,
                    "taggedBail": 12,
                    "unusedRemand": 12
                },
                "automaticRelease": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-02-03"
                },
                "conditionalRelease": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-02-03"
                },
                "dtoPostRecallRelease": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-04-01"
                },
                "earlyTerm": {
                    "date": "2020-02-03",
                    "overrideDate": "2019-04-02",
                    "calculatedDate": "2019-04-02"
                },
                "homeDetentionCurfew": {
                    "actualDate": "2020-02-03",
                    "eligibilityCalculatedDate": "2020-02-03",
                    "eligibilityDate": "2020-02-03",
                    "eligibilityOverrideDate": "2020-02-03",
                    "endDate": "2019-04-01"
                },
                "lateTerm": {
                    "date": "2020-02-03",
                    "overrideDate": "2019-04-02",
                    "calculatedDate": "2019-04-02"
                },
                "licenceExpiry": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-02-03",
                    "calculatedDate": "2020-02-03"
                },
                "midTerm": {
                    "date": "2020-02-03",
                    "overrideDate": "2019-04-02",
                    "calculatedDate": "2019-04-02"
                },
                "nonDto": {
                    "date": "2020-04-01",
                    "releaseDateType": "CRD"
                },
                "nonParole": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-02-03"
                },
                "paroleEligibility": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-02-03",
                    "calculatedDate": "2020-02-03"
                },
                "postRecallRelease": {
                    "date": "2020-02-03",
                    "overrideDate": "2020-04-01"
                },
                "release": {
                    "date": "2020-04-01",
                    "confirmedDate": "2020-04-20"
                },
                "sentence": {
                    "effectiveEndDate": "2020-02-03",
                    "expiryCalculatedDate": "2020-02-03",
                    "expiryDate": "2020-02-03",
                    "expiryOverrideDate": "2020-02-03",
                    "startDate": "2010-02-03"
                },
                "topupSupervision": {
                    "expiryCalculatedDate": "2020-02-03",
                    "expiryDate": "2020-02-03",
                    "expiryOverrideDate": "2020-02-03",
                    "startDate": "2019-04-01"
                },
                "actualParoleDate": "2020-02-03",
                "earlyRemovalSchemeEligibilityDate": "2020-02-03",
                "releaseOnTemporaryLicenceDate": "2020-02-03",
                "tariffDate": "2020-02-03",
                "tariffEarlyRemovalSchemeEligibilityDate": "2020-02-03"
            }
        }
        """.removeWhitespaceAndNewlines(),
      )
    }
  },
)
