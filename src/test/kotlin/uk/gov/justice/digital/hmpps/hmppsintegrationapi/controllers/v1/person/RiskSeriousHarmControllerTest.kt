package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.LaoRedactorAspect
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.OtherRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRiskSeriousHarmForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@WebMvcTest(controllers = [RiskSeriousHarmController::class])
@Import(value = [AopAutoConfiguration::class, LaoRedactorAspect::class])
@ActiveProfiles("test")
internal class RiskSeriousHarmControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getRiskSeriousHarmForPersonService: GetRiskSeriousHarmForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
) : DescribeSpec(
    {
      val hmppsId = "9999/11111A"
      val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
      val path = "/v1/persons/$encodedHmppsId/risks/serious-harm"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)

      describe("GET $path") {
        beforeTest {
          Mockito.reset(getRiskSeriousHarmForPersonService)
          whenever(getRiskSeriousHarmForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                Risks(
                  assessedOn =
                    LocalDateTime.of(
                      2023,
                      9,
                      19,
                      12,
                      51,
                      38,
                    ),
                  riskToSelf =
                    RiskToSelf(
                      suicide = Risk(risk = "NO"),
                      selfHarm = Risk(risk = "NO"),
                      custody = Risk(risk = "NO"),
                      hostelSetting = Risk(risk = "NO"),
                      vulnerability = Risk(risk = "NO"),
                    ),
                  otherRisks = OtherRisks(breachOfTrust = "NO"),
                  summary =
                    RiskSummary(
                      overallRiskLevel = "LOW",
                      whoIsAtRisk = "X, Y and Z are at risk",
                      natureOfRisk = "The nature of the risk is X",
                      riskImminence = "the risk is imminent and more probably in X situation",
                      riskIncreaseFactors = "If offender in situation X the risk can be higher",
                      riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
                      riskInCommunity =
                        mapOf(
                          "children" to "HIGH",
                          "public" to "HIGH",
                          "knownAdult" to "HIGH",
                          "staff" to "MEDIUM",
                          "prisoners" to "LOW",
                        ),
                      riskInCustody =
                        mapOf(
                          "children" to "LOW",
                          "public" to "LOW",
                          "knownAdult" to "HIGH",
                          "staff" to "VERY_HIGH",
                          "prisoners" to "VERY_HIGH",
                        ),
                    ),
                ),
            ),
          )
          Mockito.reset(auditService)
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("gets the risks for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getRiskSeriousHarmForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("returns the risks for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": {
            "assessedOn": "2023-09-19T12:51:38",
            "riskToSelf": {
              "suicide": {
                 "risk": "NO",
                 "previous": null,
                 "previousConcernsText": null,
                 "current": null,
                 "currentConcernsText": null
              },
              "selfHarm": {
                 "risk": "NO",
                 "previous": null,
                 "previousConcernsText": null,
                 "current": null,
                 "currentConcernsText": null
              },
              "custody": {
                 "risk": "NO",
                 "previous": null,
                 "previousConcernsText": null,
                 "current": null,
                 "currentConcernsText": null
              },
              "hostelSetting": {
                 "risk": "NO",
                 "previous": null,
                 "previousConcernsText": null,
                 "current": null,
                 "currentConcernsText": null
              },
              "vulnerability": {
                 "risk": "NO",
                 "previous": null,
                 "previousConcernsText": null,
                 "current": null,
                 "currentConcernsText": null
              }
            },
            "otherRisks": {
              "escapeOrAbscond": null,
              "controlIssuesDisruptiveBehaviour": null,
              "breachOfTrust": "NO",
              "riskToOtherPrisoners": null
            },
            "summary": {
              "whoIsAtRisk": "X, Y and Z are at risk",
              "natureOfRisk": "The nature of the risk is X",
              "riskImminence": "the risk is imminent and more probably in X situation",
              "riskIncreaseFactors": "If offender in situation X the risk can be higher",
              "riskMitigationFactors": "Giving offender therapy in X will reduce the risk",
              "overallRiskLevel": "LOW",
              "riskInCommunity": {
                "children":"HIGH",
                "public":"HIGH",
                "knownAdult":"HIGH",
                "staff":"MEDIUM",
                "prisoners":"LOW"
               },
               "riskInCustody": {
                "children":"LOW",
                "public":"LOW",
                "knownAdult":"HIGH",
                "staff":"VERY_HIGH",
                "prisoners":"VERY_HIGH"
               }
            }
          }
          """.removeWhitespaceAndNewlines(),
          )
        }

        it("Returns 403 Forbidden for LAO case") {
          val laoCrn = "B123456"
          whenever(getCaseAccess.getAccessFor(laoCrn)).thenReturn(CaseAccess(laoCrn, true, false, "Exclusion Message"))
          val result = mockMvc.performAuthorised("/v1/persons/$laoCrn/risks/serious-harm")
          result.response.status.shouldBe(HttpStatus.FORBIDDEN.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_PERSON_RISK", mapOf("hmppsId" to hmppsId))
        }

        it("returns null embedded in a JSON object when no risks are found") {
          val hmppsIdForPersonWithNoRisks = "0000/11111A"
          val encodedHmppsIdForPersonWithNoRisks = URLEncoder.encode(hmppsIdForPersonWithNoRisks, StandardCharsets.UTF_8)
          val pathForPersonWithNoRisks = "/v1/persons/$encodedHmppsIdForPersonWithNoRisks/risks/serious-harm"

          whenever(getRiskSeriousHarmForPersonService.execute(hmppsIdForPersonWithNoRisks)).thenReturn(Response(data = null))

          val result = mockMvc.performAuthorised(pathForPersonWithNoRisks)

          result.response.contentAsString.shouldContain("\"data\":null")
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getRiskSeriousHarmForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = null,
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns a 500 INTERNAL SERVER ERROR status code when upstream api return expected error") {

          whenever(getRiskSeriousHarmForPersonService.execute(hmppsId)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val result = mockMvc.performAuthorised(path)
          assert(result.response.status == 500)
          assert(
            result.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }
      }
    },
  )
