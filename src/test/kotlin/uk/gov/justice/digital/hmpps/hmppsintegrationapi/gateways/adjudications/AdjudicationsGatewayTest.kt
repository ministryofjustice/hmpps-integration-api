package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.adjudications

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AdjudicationsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AdjudicationsGateway::class],
)
class AdjudicationsGatewayTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val adjudicationsGateway: AdjudicationsGateway,
) : DescribeSpec(
  {
    val adjudicationsApiMockServer = AdjudicationsApiMockServer()
    beforeEach {
      adjudicationsApiMockServer.start()

      Mockito.reset(hmppsAuthGateway)
      whenever(hmppsAuthGateway.getClientToken("Adjudications")).thenReturn(
        HmppsAuthMockServer.TOKEN,
      )
    }
    afterTest {
      adjudicationsApiMockServer.stop()
    }

    it("authenticates using HMPPS Auth with credentials") {
      adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")

      verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("Adjudications")
    }

    it("upstream API returns an error, return error") {
      adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson("123", "", HttpStatus.BAD_REQUEST)
      val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
      response.data.shouldBe(emptyList())
      response.errors[0].type.shouldBe(UpstreamApiError.Type.BAD_REQUEST)
    }

    it("returns adjudicationResponse") {
      adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson(
        "123",
        """
        [
          {
            "chargeNumber": "string",
            "prisonerNumber": "G2996UX",
            "gender": "MALE",
            "incidentDetails": {
              "locationId": 0,
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
                  "id": 123,
                  "locationId": 123,
                  "dateTimeOfHearing": "2021-07-05T10:35:17",
                  "oicHearingType": "GOV_ADULT"
                },
                "outcome": {
                  "id": 123,
                  "adjudicator": "string",
                  "code": "COMPLETE",
                  "reason": "LEGAL_ADVICE",
                  "details": "string",
                  "plea": "UNFIT"
                }
              },
              {
                "hearing": {
                  "id": 456,
                  "locationId": 456,
                  "dateTimeOfHearing": "2021-07-05T10:35:18",
                  "oicHearingType": "GOV_ADULT"
                },
                "outcome": {
                  "id": 456,
                  "adjudicator": "string",
                  "code": "REFER_POLICE",
                  "reason": "ANOTHER_WAY",
                  "details": "string",
                  "plea": "FIT"
                }
              }
            ]
          }
        ]
        """,
        HttpStatus.OK,
      )
      val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
      response.data.count().shouldBe(1)
      response.data.first().incidentDetails?.dateTimeOfIncident.shouldBe("2021-07-05T10:35:17")
    }
  },
)
