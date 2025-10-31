package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import org.hamcrest.Matchers.aMapWithSize
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import java.io.File

@ActiveProfiles("integration-test-redaction-enabled")
@TestPropertySource(properties = ["services.ndelius.base-url=http://localhost:4201"])
class PersonRedactionIntegrationTest : IntegrationTestBase() {
  @MockitoBean
  lateinit var telemetryService: TelemetryService

  @AfterEach
  fun resetValidators() {
    prisonerOffenderSearchMockServer.resetValidator()
  }

  @BeforeEach
  fun resetMocks() {
    nDeliusMockServer.resetAll()
    prisonerOffenderSearchMockServer.resetAll()

    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsId",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )
    prisonerOffenderSearchMockServer.stubForGet(
      "/prisoner/$nomsIdFromProbation",
      File(
        "$gatewaysFolder/prisoneroffendersearch/fixtures/PrisonerByIdResponse.json",
      ).readText(),
    )

    nDeliusMockServer.stubForPost(
      "/search/probation-cases",
      writeAsJson(mapOf("crn" to crn)),
      File(
        "$gatewaysFolder/ndelius/fixtures/GetOffenderResponse.json",
      ).readText(),
    )
  }

  @Nested
  inner class GetPerson {
    @Nested
    @DisplayName("And Redaction is required")
    inner class AndRedactionIsRequired {
      private val clientNameWithRedaction = "role-based-redacted-client"

      @Test
      fun `return a person from Prisoner Offender Search with some data redacted`() {
        callApiWithCN("$basePath/$crn", clientNameWithRedaction)
          .andExpect(status().isOk)
          .andExpect(content().json(getExpectedResponse("person-offender-and-probation-search-redacted-response")))

        prisonerOffenderSearchMockServer.assertValidationPassed()
      }
    }

    @Nested
    @DisplayName("And Role based Redaction is required")
    inner class AndRoleBasedRedactionIsRequired {
      private val clientNameWithRoleBaseRedaction = "role-based-redacted-client"

      @Test
      fun `return a person from Prisoner Offender Search with redacted and removed data`() {
        callApiWithCN("$basePath/$crn", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          .andExpect(jsonPath("$.data", aMapWithSize<Any, Any>(1)))
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.identifiers.croNumber").value("**REDACTED**"))
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.pncId").value("**REDACTED**"))
          .andExpect(jsonPath("$.data.probationOffenderSearch").doesNotExist())

        verify(telemetryService, times(1)).trackEvent(
          "RedactionEvent",
          mapOf(
            "policyName" to "prison-education",
            "clientId" to "role-based-redacted-client",
            "masks" to "0",
            "removes" to "1",
            "rejects" to "0",
          ),
        )
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }
    }
  }
}
