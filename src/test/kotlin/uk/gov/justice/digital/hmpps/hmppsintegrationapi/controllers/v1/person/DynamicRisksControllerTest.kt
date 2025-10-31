package uk.gov.justice.digital.hmpps.hmppsintegrationapi.controllers.v1.person

import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.mockito.Mockito
import org.mockito.internal.verification.VerificationModeFactory
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.WebMvcTestConfiguration
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.helpers.IntegrationAPIMockMvc
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.GetCaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.limitedaccess.redactor.Redactor
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.DynamicRisk
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.Response
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApi
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.hmpps.UpstreamApiError
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.ndelius.CaseAccess
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetDynamicRisksForPersonService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.internal.AuditService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService

@WebMvcTest(controllers = [DynamicRisksController::class])
@Import(value = [WebMvcTestConfiguration::class])
@ActiveProfiles("test-redaction-enabled")
internal class DynamicRisksControllerTest(
  @Autowired var springMockMvc: MockMvc,
  @MockitoBean val getDynamicRisksForPersonService: GetDynamicRisksForPersonService,
  @MockitoBean val auditService: AuditService,
  @MockitoBean val getCaseAccess: GetCaseAccess,
  @MockitoBean val featureFlagConfig: FeatureFlagConfig,
  @MockitoBean val telemetryService: TelemetryService,
) : DescribeSpec(
    {
      val hmppsId = "A1234AA"
      val path = "/v1/persons/$hmppsId/risks/dynamic"
      val mockMvc = IntegrationAPIMockMvc(springMockMvc)
      val laoRedactCrn = "R123456"
      val laoOkCrn = "R654321"
      val laoFailureCrn = "R754321"

      describe("GET $path") {
        beforeTest {
          mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
          every { roles["full-access"] } returns testRoleWithLaoRedactions
          Mockito.reset(getDynamicRisksForPersonService)
          Mockito.reset(auditService)
          Mockito.reset(telemetryService)
          whenever(getCaseAccess.getAccessFor(any())).thenReturn(CaseAccess(laoOkCrn, false, false, "", ""))
          whenever(getCaseAccess.getAccessFor("R754321")).thenReturn(null)
          whenever(getDynamicRisksForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                listOf(
                  DynamicRisk(
                    code = "AVIS",
                    description = "Subject has a ViSOR record",
                    startDate = "2023-09-08",
                    reviewDate = "2026-04-29",
                    notes = "Nothing to say",
                  ),
                  DynamicRisk(
                    code = "RHRH",
                    description = "High Risk of Harm",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )
        }

        afterTest {
          unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        }

        it("returns a 200 OK status code") {
          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.OK.value())
        }

        it("logs audit") {
          mockMvc.performAuthorised(path)

          verify(
            auditService,
            VerificationModeFactory.times(1),
          ).createEvent("GET_DYNAMIC_RISKS", mapOf("hmppsId" to hmppsId))
        }

        it("gets the dynamic risks for a person with the matching ID") {
          mockMvc.performAuthorised(path)

          verify(getDynamicRisksForPersonService, VerificationModeFactory.times(1)).execute(hmppsId)
        }

        it("returns the dynamic risks for a person with the matching ID") {
          val result = mockMvc.performAuthorised(path)

          result.response.contentAsString.shouldContain(
            """
          "data": [
            {
              "code": "AVIS",
              "description": "Subject has a ViSOR record",
              "startDate": "2023-09-08",
              "reviewDate": "2026-04-29",
              "notes": "Nothing to say"
            },
            {
              "code": "RHRH",
              "description": "High Risk of Harm",
              "startDate": "2022-09-01",
              "reviewDate": "2024-12-23",
              "notes": "A lot of notes"
            }
          ]
        """.removeWhitespaceAndNewlines(),
          )
        }

        it("returns the redacted dynamic risks for a person with the matching identifier") {

          whenever(getCaseAccess.getAccessFor(laoRedactCrn)).thenReturn(CaseAccess(laoRedactCrn, true, true, "Exclusion Message", "Restriction Message"))
          whenever(getDynamicRisksForPersonService.execute(laoRedactCrn)).thenReturn(
            Response(
              data =
                listOf(
                  DynamicRisk(
                    code = "AVIS",
                    description = "Subject has a ViSOR record",
                    startDate = "2023-09-08",
                    reviewDate = "2026-04-29",
                    notes = "Nothing to say",
                  ),
                  DynamicRisk(
                    code = "RHRH",
                    description = "High Risk of Harm",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised("/v1/persons/$laoRedactCrn/risks/dynamic")

          result.response.contentAsString.shouldContain(
            """
          "data": [
            {
              "code": "AVIS",
              "description": "Subject has a ViSOR record",
              "startDate": "2023-09-08",
              "reviewDate": "2026-04-29",
              "notes": "${Redactor.REDACTED}"
            },
            {
              "code": "RHRH",
              "description": "High Risk of Harm",
              "startDate": "2022-09-01",
              "reviewDate": "2024-12-23",
              "notes": "${Redactor.REDACTED}"
            }
          ]
        """.removeWhitespaceAndNewlines(),
          )

          // Verify a telemetry event has been written
          // 2 masks on the notes fields
          verify(telemetryService, times(1)).trackEvent(
            "RedactionEvent",
            mapOf("policyName" to "lao-redactions", "clientId" to "automated-test-client", "masks" to "2", "removes" to "0", "rejects" to "0"),
          )
        }

        it("returns an empty list when no dynamic risks are found") {
          val hmppsIdForPersonWithNoDynamicRisks = "A1234AA"
          val dynamicRisksPath = "/v1/persons/$hmppsIdForPersonWithNoDynamicRisks/risks/dynamic"

          whenever(getDynamicRisksForPersonService.execute(hmppsIdForPersonWithNoDynamicRisks)).thenReturn(
            Response(
              data = emptyList(),
            ),
          )

          val result = mockMvc.performAuthorised(dynamicRisksPath)

          result.response.contentAsString.shouldContain("\"data\":[]".removeWhitespaceAndNewlines())
        }

        it("returns a 404 NOT FOUND status code when person isn't found in the upstream API") {
          whenever(getDynamicRisksForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data = emptyList(),
              errors =
                listOf(
                  UpstreamApiError(
                    causedBy = UpstreamApi.PRISON_API,
                    type = UpstreamApiError.Type.ENTITY_NOT_FOUND,
                  ),
                ),
            ),
          )

          val result = mockMvc.performAuthorised(path)

          result.response.status.shouldBe(HttpStatus.NOT_FOUND.value())
        }

        it("returns paginated results") {
          whenever(getDynamicRisksForPersonService.execute(hmppsId)).thenReturn(
            Response(
              data =
                List(20) {
                  DynamicRisk(
                    code = "XNR",
                    description = "Not For Release",
                    startDate = "2022-08-01",
                    reviewDate = "2025-08-01",
                    notes = "Notes all written here",
                  )
                },
            ),
          )

          val result = mockMvc.performAuthorised("$path?page=1&perPage=10")

          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.page", 1)
          result.response.contentAsString.shouldContainJsonKeyValue("$.pagination.totalPages", 2)
        }

        it("fails with the appropriate error when an upstream service is down") {
          whenever(getDynamicRisksForPersonService.execute(hmppsId)).doThrow(
            WebClientResponseException(500, "MockError", null, null, null, null),
          )

          val response = mockMvc.performAuthorised("$path?page=1&perPage=10")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"500 MockError\",\"developerMessage\":\"Unable to complete request as an upstream service is not responding\",\"moreInfo\":null}",
            ),
          )
        }

        it("fails with the appropriate error when LAO context has failed to be retrieved") {

          whenever(getDynamicRisksForPersonService.execute(laoFailureCrn)).thenReturn(
            Response(
              data =
                listOf(
                  DynamicRisk(
                    code = "AVIS",
                    description = "Subject has a ViSOR record",
                    startDate = "2023-09-08",
                    reviewDate = "2026-04-29",
                    notes = "Nothing to say",
                  ),
                  DynamicRisk(
                    code = "RHRH",
                    description = "High Risk of Harm",
                    startDate = "2022-09-01",
                    reviewDate = "2024-12-23",
                    notes = "A lot of notes",
                  ),
                ),
            ),
          )

          val response = mockMvc.performAuthorised("/v1/persons/$laoFailureCrn/risks/dynamic")

          assert(response.response.status == 500)
          assert(
            response.response.contentAsString.equals(
              "{\"status\":500,\"errorCode\":null,\"userMessage\":\"LAO Check failed\",\"developerMessage\":\"LAO Check failed\",\"moreInfo\":null}",
            ),
          )
        }
      }
    },
  )
