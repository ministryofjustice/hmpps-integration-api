package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.hamcrest.Matchers.aMapWithSize
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.verification.VerificationMode
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.result.JsonPathResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.MockMvcExtensions.writeAsJson
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.removeWhitespaceAndNewlines
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
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
    @DisplayName("And Role based Redaction is NOT required")
    inner class AndRedactionIsNotRequired {
      @Test
      fun `return a person from Prisoner Offender Search without redaction`() {
        callApi("$basePath/$crn")
          .andExpect(status().isOk)
          .andExpect(jsonPath("$.data.prisonerOffenderSearch").exists())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.middleName").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.aliases[*].middleName").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.identifiers.croNumber").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.identifiers.deliusCrn").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.pncId").isNotRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch").exists())

        verifyRedactionEvent("prison-education", "role-based-redacted-client", never())
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }
    }

    @Nested
    @DisplayName("And Role based Redaction is required")
    inner class AndRoleBasedRedactionIsRequired {
      private val clientNameWithRoleBaseRedaction = "role-based-redacted-client"

      @Test
      fun `return a person from Prisoner Offender Search with some data redacted`() {
        callApiWithCN("$basePath/$crn", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          .andExpect(content().json(getExpectedResponse("person-offender-and-probation-search-redacted-response")))

        verifyRedactionEvent("prison-education", "role-based-redacted-client", times(1), removes = 1)
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }

      @Test
      fun `return a person from Prisoner Offender Search with redacted and removed data`() {
        callApiWithCN("$basePath/$crn", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          .andExpect(jsonPath("$.data", aMapWithSize<Any, Any>(1)))
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.middleName").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.aliases[*].middleName").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.identifiers.croNumber").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.identifiers.deliusCrn").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.pncId").isRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch").doesNotExist())

        verifyRedactionEvent("prison-education", "role-based-redacted-client", times(1), removes = 1)
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }
    }

    private fun verifyRedactionEvent(
      policyName: String,
      clientId: String,
      verificationMode: VerificationMode = times(1),
      masks: Int? = 0,
      removes: Int? = 0,
      rejects: Int? = 0,
    ) = verify(telemetryService, verificationMode).trackEvent(
      "RedactionEvent",
      mapOf(
        "policyName" to policyName,
        "clientId" to clientId,
        "masks" to "$masks",
        "removes" to "$removes",
        "rejects" to "$rejects",
      ),
    )

    // Handy local function (extensions) for verifying redacted strings.
    private fun JsonPathResultMatchers.isRedacted() = value("**REDACTED**")

    // Handy local function (extensions) for verifying strings not redacted
    private fun JsonPathResultMatchers.isNotRedacted() = value(IsNot(IsEqual("**REDACTED**")))

    @Nested
    @DisplayName("And Role based LAO Redaction is required")
    inner class AndLaoRedactionIsRequired {
      private val clientNameWithRoleBaseRedaction = "lao-role-based-redacted-client"

      @BeforeEach
      fun setUp() {
        nDeliusMockServer.stubForPost(
          "/probation-cases/access",
          """
          {
            "crns": ["A123456"]
          }
          """.removeWhitespaceAndNewlines(),
          """
          {
            "access": [{
              "crn": "A123456",
              "userExcluded": true,
              "userRestricted": false
            }]
          }
          """.trimIndent(),
        )
        mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        every { roles["test-role"] } returns testRoleWithLaoRedactions
      }

      @Test
      fun `return a person with redacted and removed data`() {
        callApiWithCN("$basePath/$crn", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          .andExpect(jsonPath("$.data", aMapWithSize<Any, Any>(2)))
          .andExpect(jsonPath("$.data.probationOffenderSearch.middleName").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.middleName").isRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch.gender").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.gender").isRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch.ethnicity").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.ethnicity").isRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch.contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data.probationOffenderSearch.aliases[*].middleName").isRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.aliases[*].middleName").isRedacted())

        // Expect 4 masks on middle name
        // Expect 4 masks on gender
        // Expect 4 masks on ethnicity
        verifyRedactionEvent("lao-redactions", "lao-role-based-redacted-client", times(3), masks = 4)
        // Expect 2 removes on contact details name
        verifyRedactionEvent("lao-redactions", "lao-role-based-redacted-client", times(1), removes = 2)
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }

      @Test
      fun `rejects person address`() {
        callApiWithCN("$basePath/$crn/addresses", clientNameWithRoleBaseRedaction)
          .andExpect(status().isForbidden)
      }
    }

    @AfterEach
    fun tearDown() {
      unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      nDeliusMockServer.resetAll()
    }
  }
}
