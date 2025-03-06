package uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.assessrisksandneeds

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.booleans.shouldBeTrue
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.AssessRisksAndNeedsGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.HmppsAuthGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.ApiMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.mockservers.HmppsAuthMockServer
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Need
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Needs
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import java.time.LocalDateTime

@ActiveProfiles("test")
@ContextConfiguration(
  initializers = [ConfigDataApplicationContextInitializer::class],
  classes = [AssessRisksAndNeedsGateway::class],
)
class GetNeedsForPersonTest(
  @MockitoBean val hmppsAuthGateway: HmppsAuthGateway,
  val assessRisksAndNeedsGateway: AssessRisksAndNeedsGateway,
) : DescribeSpec(
    {
      val deliusCrn = "X777776"
      val path = "/needs/crn/$deliusCrn"
      val assessRisksAndNeedsApiMockServer = ApiMockServer.create(UpstreamApi.ASSESS_RISKS_AND_NEEDS)

      beforeEach {
        assessRisksAndNeedsApiMockServer.start()
        Mockito.reset(hmppsAuthGateway)
        assessRisksAndNeedsApiMockServer.stubForGet(
          path,
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
          Needs(
            assessedOn = LocalDateTime.of(2023, 2, 13, 12, 43, 38),
            identifiedNeeds =
              listOf(
                Need(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
                Need(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
              ),
            notIdentifiedNeeds =
              listOf(
                Need(type = "RELATIONSHIPS"),
              ),
            unansweredNeeds =
              listOf(
                Need(type = "LIFESTYLE_AND_ASSOCIATES"),
                Need(type = "DRUG_MISUSE"),
                Need(type = "ALCOHOL_MISUSE"),
              ),
          ),
        )
      }

      it("returns an empty list when a needs section has no data") {
        assessRisksAndNeedsApiMockServer.stubForGet(
          path,
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
          Needs(
            assessedOn = LocalDateTime.of(2023, 2, 13, 12, 43, 38),
            identifiedNeeds =
              listOf(
                Need(type = "EDUCATION_TRAINING_AND_EMPLOYABILITY"),
                Need(type = "FINANCIAL_MANAGEMENT_AND_INCOME"),
              ),
            notIdentifiedNeeds = emptyList(),
            unansweredNeeds = emptyList(),
          ),
        )
      }

      it("returns an error when 404 NOT FOUND is returned because no person is found") {
        assessRisksAndNeedsApiMockServer.stubForGet(path, "", HttpStatus.NOT_FOUND)

        val response = assessRisksAndNeedsGateway.getNeedsForPerson(deliusCrn)

        response.hasError(UpstreamApiError.Type.ENTITY_NOT_FOUND).shouldBeTrue()
      }
    },
  )
