package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.adjudications

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AdjudicationsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AdjudicationsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AdjudicationsGateway::class],
)
class AdjudicationsGatewayTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
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

      it("returns an error when 400 Bad Request is returned because of invalid ID") {
        adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson("123", "", HttpStatus.BAD_REQUEST)
        val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")

        response.errors
          .first()
          .type
          .shouldBe(UpstreamApiError.Type.BAD_REQUEST)
        response.errors
          .first()
          .causedBy
          .shouldBe(UpstreamApi.ADJUDICATIONS)
      }

      it("returns adjudicationResponse") {
        adjudicationsApiMockServer.stubGetReportedAdjudicationsForPerson(
          "123",
          """
        [
          {
            "incidentDetails": {
              "dateTimeOfIncident": "2021-07-05T10:35:17"
            },
            "isYouthOffender": true,
            "incidentRole": {
              "roleCode": "25a",
              "offenceRule": {
                "paragraphNumber": "25(a)",
                "paragraphDescription": "Committed an assault"
              }
            },
            "offenceDetails": {
              "offenceCode": 3,
              "offenceRule": {
                "paragraphNumber": "25(a)",
                "paragraphDescription": "Committed an assault"
              }
            },
            "hearings": [
              {
                "dateTimeOfHearing": "2021-07-05T10:35:17",
                "oicHearingType": "GOV_ADULT",
                "outcome": {
                  "code": "COMPLETE",
                  "reason": "LEGAL_ADVICE",
                  "details": "string",
                  "plea": "UNFIT"
                }
              }
            ],
            "outcomes": [
              {
                "hearing": {
                  "dateTimeOfHearing": "2021-07-05T10:35:17",
                  "oicHearingType": "GOV_ADULT",
                  "outcome": {
                    "code": "COMPLETE",
                    "reason": "LEGAL_ADVICE",
                    "details": "string",
                    "plea": "UNFIT"
                  }
                },
                "outcome": {
                  "outcome": {
                    "code": "REFER_POLICE",
                    "details": "string",
                    "reason": "ANOTHER_WAY",
                    "quashedReason": "FLAWED_CASE",
                    "canRemove": true
                  },
                  "referralOutcome": {
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
                "type": "PRIVILEGE",
                "privilegeType": "CANTEEN",
                "otherPrivilege": "string",
                "schedule": {
                  "days": 648,
                  "startDate": "2019-08-24",
                  "endDate": "2019-08-24",
                  "suspendedUntil": "2019-08-24"
                }
              }
            ],
            "punishmentComments": [
              {
                "comment": "string",
                "reasonForChange": "APPEAL",
                "dateTime": "2021-07-05T10:35:17"
              }
            ]
          }
        ]
        """,
          HttpStatus.OK,
        )
        val response = adjudicationsGateway.getReportedAdjudicationsForPerson(id = "123")
        response.data.count().shouldBe(1)
        response.data
          .first()
          .incidentDetails
          ?.dateTimeOfIncident
          .shouldBe("2021-07-05T10:35:17")
      }
    },
  )
