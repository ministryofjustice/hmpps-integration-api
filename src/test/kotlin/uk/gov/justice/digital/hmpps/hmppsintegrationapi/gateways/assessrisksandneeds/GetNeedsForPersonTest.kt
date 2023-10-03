package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.AssessRisksAndNeedsApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Need as IntegrationApiNeed
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Needs as IntegrationApiNeeds

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetNeedsForPersonTest(
  @MockBean val hmppsAuthGateway: HmppsAuthGateway,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) :
  DescribeSpec(
    {
      val assessRisksAndNeedsApiMockServer = AssessRisksAndNeedsApiMockServer()
      val deliusCrn = "X777776"

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubGetNeedsForPerson(
          deliusCrn,
          """
              {
                "assessedOn": "2023-02-13T12:43:38",
                "identifiedNeeds": [
                  {
                    "section": "EDUCATION_TRAINING_AND_EMPLOYABILITY"
                  },
                  {
                    "section": "FINANCIAL_MANAGEMENT_AND_INCOME"
                  }
                ],
                "notIdentifiedNeeds": [
                  {
                    "section": "RELATIONSHIPS"
                  }
                ],
                "unansweredNeeds": [
                  {
                    "section": "LIFESTYLE_AND_ASSOCIATES"
                  },
                  {
                    "section": "DRUG_MISUSE"
                  },
                  {
                    "section": "ALCOHOL_MISUSE"
                  }
                ]
              }
          """,
        )

        Mockito.reset(hmppsAuthGateway)
        whenever(hmppsAuthGateway.getClientToken("ASSESS_RISKS_AND_NEEDS")).thenReturn(HmppsAuthMockServer.TOKEN)
      }

      afterTest {
        assessRisksAndNeedsApiMockServer.stop()
      }

      it("authenticates using HMPPS Auth with credentials") {
        assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)

        verify(hmppsAuthGateway, VerificationModeFactory.times(1)).getClientToken("ASSESS_RISKS_AND_NEEDS")
      }

      it("returns needs for the person with the matching CRN") {
        val response = assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)

        response.data.shouldBe(
          IntegrationApiNeeds(
            assessedOn = LocalDateTime.of(2023, 2, 13, 12, 43, 38),
            identifiedNeeds = listOf(
              IntegrationApiNeed(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
              IntegrationApiNeed(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
            ),
            notIdentifiedNeeds = listOf(
              IntegrationApiNeed(type = "RELATIONSHIPS"),
            ),
            unansweredNeeds = listOf(
              IntegrationApiNeed(type = "LIFESTYLE_AND_ASSOCIATES"),
              IntegrationApiNeed(type = "DRUG_MISUSE"),
              IntegrationApiNeed(type = "ALCOHOL_MISUSE"),
            ),
          ),
        )
      }

      it("returns an empty list when a needs section has no data") {
        assessRisksAndNeedsApiMockServer.stubGetNeedsForPerson(
          deliusCrn,
          """
           {
              "assessedOn": "2023-02-13T12:43:38",
              "identifiedNeeds": [
                {
                  "section": "EDUCATION_TRAINING_AND_EMPLOYABILITY"
                },
                {
                  "section": "FINANCIAL_MANAGEMENT_AND_INCOME"
                }
              ],
              "notIdentifiedNeeds": [],
              "unansweredNeeds": []
           }
          """,
        )

        val response = assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)

        response.data.shouldBe(
          IntegrationApiNeeds(
            assessedOn = LocalDateTime.of(2023, 2, 13, 12, 43, 38),
            identifiedNeeds = listOf(
              IntegrationApiNeed(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
              IntegrationApiNeed(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
            ),
            notIdentifiedNeeds = emptyList(),
            unansweredNeeds = emptyList(),
          ),
        )
      }

      it("returns an error when 404 NOT FOUND is returned because no person is found") {
        assessRisksAndNeedsApiMockServer.stubGetNeedsForPerson(deliusCrn, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
