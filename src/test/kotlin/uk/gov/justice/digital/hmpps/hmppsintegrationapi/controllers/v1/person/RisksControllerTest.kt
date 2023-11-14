package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.OtherRisks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.RiskToSelf
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.Risks
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetRisksForPersonService
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime

@WebMvcTest(controllers = [RisksController::class])
internal class RisksControllerTest(
  @Autowired val mockMvc: MockMvc,
  @MockBean val getRisksForPersonService: GetRisksForPersonService,
) : DescribeSpec(
  {
    val hmppsId = "9999/11111A"
    val encodedHmppsId = URLEncoder.encode(hmppsId, StandardCharsets.UTF_8)
    val path = "/v1/persons/$encodedHmppsId/risks"

    describe("GET $path") {
      beforeTest {
        Mockito.reset(getRisksForPersonService)
        whenever(getRisksForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = Risks(
              assessedOn = LocalDateTime.of(
                2023,
                9,
                19,
                12,
                51,
                38,
              ),
              riskToSelf = RiskToSelf(
                suicide = Risk(risk = "NO"),
                selfHarm = Risk(risk = "NO"),
                custody = Risk(risk = "NO"),
                hostelSetting = Risk(risk = "NO"),
                vulnerability = Risk(risk = "NO"),
              ),
              otherRisks = OtherRisks(breachOfTrust = "NO"),
              summary = RiskSummary(
                overallRiskLevel = "LOW",
                whoIsAtRisk = "X, Y and Z are at risk",
                natureOfRisk = "The nature of the risk is X",
                riskImminence = "the risk is imminent and more probably in X situation",
                riskIncreaseFactors = "If offender in situation X the risk can be higher",
                riskMitigationFactors = "Giving offender therapy in X will reduce the risk",
                riskInCommunity = mapOf(
                  "children" to "HIGH",
                  "public" to "HIGH",
                  "knownAdult" to "HIGH",
                  "staff" to "MEDIUM",
                  "prisoners" to "LOW",
                ),
                riskInCustody = mapOf(
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
      }

      it("responds with a 200 OK status") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        result.response.status.shouldBe(HttpStatus.OK.value())
      }

      it("retrieves the risks for a person with the matching ID") {
        mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        verify(getRisksForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
      }

      it("returns the risks for a person with the matching ID") {
        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

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

      it("returns null embedded in a JSON object when no risks are found") {
        val hmppsIdForPersonWithNoRisks = "0000/11111A"
        val encodedHmppsIdForPersonWithNoRisks = URLEncoder.encode(hmppsIdForPersonWithNoRisks, StandardCharsets.UTF_8)
        val pathForPersonWithNoRisks = "/v1/persons/$encodedHmppsIdForPersonWithNoRisks/risks"

        whenever(getRisksForPersonService.execute(hmppsIdForPersonWithNoRisks)).thenReturn(Response(data = null))

        val result = mockMvc.perform(MockMvcRequestBuilders.get(pathForPersonWithNoRisks)).andReturn()

        result.response.contentAsString.shouldContain("\"data\":null")
      }

      it("responds with a 404 NOT FOUND status when person isn't found in the upstream API") {
        whenever(getRisksForPersonService.execute(hmppsId)).thenReturn(
          Response(
            data = null,
            errors = listOf(
              UpstreamApiError(
                causedBy = UpstreamApi.ASSESS_RISKS_AND_NEEDS,
                type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
              ),
            ),
          ),
        )

        val result = mockMvc.perform(MockMvcRequestBuilders.get(path)).andReturn()

        result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
      }
    }
  },
)
