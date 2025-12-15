package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration.person

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
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
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithIdOnlyRedaction
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.roles.testRoleWithLaoRedactions
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService.Companion.attributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.util.TestConstants.DEFAULT_CRN
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
            "crns": ["$crn"]
          }
          """.removeWhitespaceAndNewlines(),
          """
          {
            "access": [{
              "crn": "$crn",
              "userExcluded": true,
              "userRestricted": false
            }]
          }
          """.trimIndent(),
        )
        nDeliusMockServer.stubForPost(
          "/search/probation-cases",
          """
            {
              "firstName": "Robert",
              "surname": "Larsen",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
          File(
            "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseLao.json",
          ).readText(),
        )

        mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        every { roles["test-role"] } returns testRoleWithLaoRedactions
      }

      @Test
      fun `return a person with redacted and removed data`() {
        callApiWithCN("$basePath/$crn", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          .andExpect(jsonPath("$.data", aMapWithSize<Any, Any>(2)))
          .andExpect(jsonPath("$.data.probationOffenderSearch.middleName").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.middleName").isNotRedacted())
          .andExpect(jsonPath("$.data.probationOffenderSearch.gender").doesNotExist())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.gender").doesNotExist())
          .andExpect(jsonPath("$.data.probationOffenderSearch.ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data.probationOffenderSearch.contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data.probationOffenderSearch.aliases[*].middleName").isNotRedacted())
          .andExpect(jsonPath("$.data.prisonerOffenderSearch.aliases[*].middleName").isNotRedacted())

        // Expect 4 removes on gender
        // Expect 4 removes on ethnicity
        verifyRedactionEvent("lao-redactions", "lao-role-based-redacted-client", times(2), removes = 4)
        // Expect 2 removes on contact details
        verifyRedactionEvent("lao-redactions", "lao-role-based-redacted-client", times(1), removes = 2)
        prisonerOffenderSearchMockServer.assertValidationPassed()
      }

      @Test
      fun `rejects person address`() {
        callApiWithCN("$basePath/$crn/addresses", clientNameWithRoleBaseRedaction)
          .andExpect(status().isForbidden)
      }

      @Test
      fun `redacts person search`() {
        val firstName = "Robert"
        val lastName = "Larsen"
        val expectedRequest = attributeSearchRequest(firstName, lastName)

        prisonerOffenderSearchMockServer.stubForPost(
          "/attribute-search",
          jacksonObjectMapper().writeValueAsString(expectedRequest.toMap()),
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearch.json",
          ).readText(),
        )

        val queryParams = "first_name=$firstName&last_name=$lastName"
        callApiWithCN("$basePath?$queryParams", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          // Entry 0 is a prison response - therefore null restriction or exclusion - REDACT
          .andExpect(jsonPath("$.data[0].middleName").isNotRedacted())
          .andExpect(jsonPath("$.data[0].gender").doesNotExist())
          .andExpect(jsonPath("$.data[0].ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data[0].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[0].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[0].aliases[*].middleName").isNotRedacted())
          // Entry 1 is probation response with false restriction and false exclusion - DO NOT REDACT
          .andExpect(jsonPath("$.data[1].middleName").isNotRedacted())
          .andExpect(jsonPath("$.data[1].gender").isNotRedacted())
          .andExpect(jsonPath("$.data[1].ethnicity").isNotRedacted())
          .andExpect(jsonPath("$.data[1].contactDetails").exists())
          .andExpect(jsonPath("$.data[1].contactDetails").exists())
          .andExpect(jsonPath("$.data[1].aliases[*].middleName").isNotRedacted())
          // Entry 2 is probation response with true restriction and false exclusion - REDACT
          .andExpect(jsonPath("$.data[2].middleName").isNotRedacted())
          .andExpect(jsonPath("$.data[2].gender").doesNotExist())
          .andExpect(jsonPath("$.data[2].ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data[2].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[2].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[2].aliases[*].middleName").isNotRedacted())
      }
    }

    @Nested
    @DisplayName("And Role based Person Search ID only Redaction is required")
    inner class AndPersonSearchIdOnlyRedactionIsRequired {
      private val clientNameWithRoleBaseRedaction = "lao-role-based-redacted-client"

      @BeforeEach
      fun setUp() {
        nDeliusMockServer.stubForPost(
          "/search/probation-cases",
          """
            {
              "firstName": "Robert",
              "surname": "Larsen",
              "includeAliases": false
            }
            """.removeWhitespaceAndNewlines(),
          File(
            "$gatewaysFolder/ndelius/fixtures/GetOffenderResponseLao.json",
          ).readText(),
        )

        mockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
        every { roles[any()] } returns testRoleWithIdOnlyRedaction
      }

      @Test
      fun `person search redacts all except ids`() {
        val firstName = "Robert"
        val lastName = "Larsen"

        val expectedRequest = attributeSearchRequest(firstName, lastName)

        prisonerOffenderSearchMockServer.stubForPost(
          "/attribute-search",
          jacksonObjectMapper().writeValueAsString(expectedRequest.toMap()),
          File(
            "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearch.json",
          ).readText(),
        )

        val queryParams = "first_name=$firstName&last_name=$lastName"
        callApiWithCN("$basePath?$queryParams", clientNameWithRoleBaseRedaction)
          .andExpect(status().isOk)
          // Removed
          .andExpect(jsonPath("$.data[0].firstName").doesNotExist())
          .andExpect(jsonPath("$.data[0].lastName").doesNotExist())
          .andExpect(jsonPath("$.data[0].middleName").doesNotExist())
          .andExpect(jsonPath("$.data[0].dateOfBirth").doesNotExist())
          .andExpect(jsonPath("$.data[0].gender").doesNotExist())
          .andExpect(jsonPath("$.data[0].ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data[0].aliases").doesNotExist())
          .andExpect(jsonPath("$.data[0].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[0].currentRestriction").doesNotExist())
          .andExpect(jsonPath("$.data[0].restrictionMessage").doesNotExist())
          .andExpect(jsonPath("$.data[0].currentExclusion").doesNotExist())
          .andExpect(jsonPath("$.data[0].exclusionMessage").doesNotExist())
          // Remaining
          .andExpect(jsonPath("$.data[0].identifiers.nomisNumber").exists())
          .andExpect(jsonPath("$.data[0].identifiers.croNumber").exists())
          .andExpect(jsonPath("$.data[0].identifiers.deliusCrn").doesNotExist())
          .andExpect(jsonPath("$.data[0].pncId").exists())
          .andExpect(jsonPath("$.data[0].hmppsId").exists())
          .andExpect(jsonPath("$.data[0].hmppsId").value("A1234AA"))
          // Removed
          .andExpect(jsonPath("$.data[2].firstName").doesNotExist())
          .andExpect(jsonPath("$.data[2].lastName").doesNotExist())
          .andExpect(jsonPath("$.data[2].middleName").doesNotExist())
          .andExpect(jsonPath("$.data[2].dateOfBirth").doesNotExist())
          .andExpect(jsonPath("$.data[2].gender").doesNotExist())
          .andExpect(jsonPath("$.data[2].ethnicity").doesNotExist())
          .andExpect(jsonPath("$.data[2].aliases").doesNotExist())
          .andExpect(jsonPath("$.data[2].contactDetails").doesNotExist())
          .andExpect(jsonPath("$.data[2].currentRestriction").doesNotExist())
          .andExpect(jsonPath("$.data[2].restrictionMessage").doesNotExist())
          .andExpect(jsonPath("$.data[2].currentExclusion").doesNotExist())
          .andExpect(jsonPath("$.data[2].exclusionMessage").doesNotExist())
          // Remaining
          .andExpect(jsonPath("$.data[2].identifiers.nomisNumber").exists())
          .andExpect(jsonPath("$.data[2].identifiers.croNumber").exists())
          .andExpect(jsonPath("$.data[2].identifiers.deliusCrn").exists())
          .andExpect(jsonPath("$.data[2].pncId").exists())
          .andExpect(jsonPath("$.data[2].hmppsId").exists())
          .andExpect(jsonPath("$.data[2].hmppsId").value(DEFAULT_CRN))
      }
    }

    @AfterEach
    fun tearDown() {
      unmockkStatic("uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.RoleKt")
      nDeliusMockServer.resetAll()
    }
  }
}
