package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.FeatureFlagConfig.Companion.PRISON_ROLE_CHECK_ENABLED
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.config.defaultObjectMapper
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.extensions.RequestContext
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.probationoffendersearch.PersonSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.GetPersonsService.Companion.attributeSearchRequest
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.services.onbehalfof.createUnsignedJwt
import java.io.File
import kotlin.collections.emptyList
import kotlin.test.Test
import kotlin.test.assertEquals

class OnBehalfOfIntegrationTest : IntegrationTestBase() {
  @BeforeEach
  fun setUp() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.OBO_VERIFY_USERNAME)).thenReturn(true)
    manageUsersMockServer.stubForGet(
      "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundResponse.json",
      ).readText(),
    )

    manageUsersMockServer.stubForGet(
      "/prisonusers/by-email/testName/details",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponse.json",
      ).readText(),
    )
  }

  @Test
  fun `if oboConfig is empty, and no Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-empty")
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is empty, and a Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-empty", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is unsigned, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-unsigned")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is unsigned, and a Jwt is provided, return ok`() {
    callApiWithCN("/v1/status", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `if oboConfig is entra, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-entra")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is entra, and an unsigned Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-entra", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is invalid, and no Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-invalid")
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `if oboConfig is invalid, and a Jwt is provided, return unauthorized`() {
    callApiWithCN("/v1/status", "obo-invalid", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `an obo username is passed to HmppsAuthGateway for the sentences endpoint using NONIS`() {
    callApiWithCN("$basePath/$crn/sentences", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("NOMIS"), requestContext.capture())
    val obUserName =
      requestContext.allValues
        .filter { it?.oboUser?.username != null }
        .map { it?.oboUser?.username }
        .first()
    assertEquals("testName", obUserName)
  }

  @Test
  fun `an obo username is passed to HmppsAuthGateway for the sentences endpoint using NDELIUS`() {
    callApiWithCN("$basePath/$crn/sentences", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("nDelius"), requestContext.capture())
    val obUserName =
      requestContext.allValues
        .filter { it?.oboUser?.username != null }
        .map { it?.oboUser?.username }
        .first()
    assertEquals("testName", obUserName)
  }

  @Test
  fun `an obo username is passed to HmppsAuthGateway for the get person endpoint using NOMIS`() {
    callApiWithCN("$basePath/$crn", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    val obUserName =
      requestContext.allValues
        .filter { it?.oboUser?.username != null }
        .map { it?.oboUser?.username }
        .first()
    assertEquals("testName", obUserName)
  }

  @Test
  fun `an obo username is passed to HmppsAuthGateway for the get person endpoint using NDELIUS`() {
    callApiWithCN("$basePath/$crn", "obo-unsigned", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("nDelius"), requestContext.capture())
    val obUserName =
      requestContext.allValues
        .filter { it?.oboUser?.username != null }
        .map { it?.oboUser?.username }
        .first()
    assertEquals("testName", obUserName)
  }

  @Test
  fun `an obo username is verified successfully`() {
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `an obo username is verified unsuccessfully beacuse it cant be found`() {
    manageUsersMockServer.stubForGet(
      "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserNotFoundResponse.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `an obo username is NOT verified when feature flag is disabled`() {
    whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.OBO_VERIFY_USERNAME)).thenReturn(false)
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
  }

  @Test
  fun `an obo username is verified unsuccessfully because it is not enabled`() {
    manageUsersMockServer.stubForGet(
      "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundNotEnabledResponse.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `an obo username is verified unsuccessfully because it is locked`() {
    manageUsersMockServer.stubForGet(
      "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundLockedResponse.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isUnauthorized)
  }

  @Test
  fun `an obo user has access to the global search role when feature is enabled`() {
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(true)
  }

  @Test
  fun `an obo user does not have access to the global search role when feature is enabled`() {
    manageUsersMockServer.stubForGet(
      "/prisonusers/by-email/testName/details",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponseDisabled.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(false)
  }

  @Test
  fun `if no obo config then obo username and role flag is null`() {
    callApiWithCN("$basePath/$crn", "obo-empty")
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(null)
    assertThat(requestContext.firstValue?.oboUser?.username).isEqualTo(null)
  }

  @Test
  fun `an obo user does not have the obo role flag set when feature is disabled`() {
    whenever(featureFlagConfig.isEnabled(PRISON_ROLE_CHECK_ENABLED)).thenReturn(false)
    manageUsersMockServer.stubForGet(
      "/prisonusers/by-email/testName/details",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponseDisabled.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(null)
  }

  @Test
  fun `an obo user does not have access to the global search role and no caseloads found - prison filter set to empty list`() {
    manageUsersMockServer.stubForGet(
      "/prisonusers/by-email/testName/details",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponseDisabled.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(false)
    assertThat(requestContext.firstValue?.filters?.prisons).isEqualTo(emptyList<String>())
  }

  @Test
  fun `an obo user does not have access to the global search role and caseloads found - prison filter set to empty list`() {
    manageUsersMockServer.stubForGet(
      "/prisonusers/by-email/testName/details",
      File(
        "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponseNoRole.json",
      ).readText(),
    )
    callApiWithCN("$basePath/$crn", "obo-unsigned-verified", oboValue = createUnsignedJwt())
      .andExpect(MockMvcResultMatchers.status().isOk)
    val requestContext = argumentCaptor<RequestContext?>()
    verify(authGateway, atLeast(1)).getClientToken(eq("Prisoner Offender Search"), requestContext.capture())
    assertThat(requestContext.firstValue?.oboUser?.hasPrisonRole).isEqualTo(false)
    assertThat(requestContext.firstValue?.filters?.prisons).isEqualTo(listOf("WWI", "MKI"))
  }

  @Nested
  inner class UserRedactionsAndFilters {
    // User LAO Redaction
    val firstName = "Robert"
    val lastName = "Larsen"
    val searchRequest = PersonSearchRequest(firstName, lastName)
    val expectedRequest = attributeSearchRequest(firstName, lastName)
    val v2Response =
      File(
        "$gatewaysFolder/cpr/fixtures/core-person-record-search-response-new.json",
      ).readText()

    val v2Request =
      File(
        "$gatewaysFolder/cpr/fixtures/core-person-record-search-request.json",
      ).readText()

    @BeforeEach
    internal fun setUp() {
      whenever(featureFlagConfig.isEnabled(FeatureFlagConfig.USE_PROBATION_SEARCH_FOR_PERSON_SEARCH)).thenReturn(true)
      probationAccessMockServer.stubForPost(
        "/user/DELIUS_USER/access",
        defaultObjectMapper.writeValueAsString(listOf("A123456")),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponse.json").readText(),
      )

      probationAccessMockServer.stubForPost(
        "/user/DELIUS_USER/access",
        defaultObjectMapper.writeValueAsString(listOf("A669783", "Z163457")),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponse.json").readText(),
      )

      prisonerOffenderSearchMockServer.stubForPost(
        "/attribute-search?page=0&size=10",
        jacksonObjectMapper().writeValueAsString(expectedRequest.toMap()),
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/prisoneroffendersearch/fixtures/AttributeSearch.json",
        ).readText(),
      )
      probationSearchMockServer.stubForPost(
        searchRequest.uriString(1, 10),
        defaultObjectMapper.writeValueAsString(searchRequest.toMap()),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationoffendersearch/fixtures/PaginatedOffenderSearchResponse.json").readText(),
      )

      manageUsersMockServer.stubForGet(
        "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundAllUsernamesResponse.json",
        ).readText(),
      )

      corePersonRecordMockServer.stubForPost(
        "/person/vetting/search",
        resBody = v2Response,
        reqBody = v2Request,
      )
    }

    @Test
    fun `an obo user has a redaction applied to v1 person search`() {
      val queryParams = "first_name=$firstName&last_name=$lastName"
      callApiWithCN("$basePath?$queryParams", "obo-unsigned-verified", oboValue = createUnsignedJwt())
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-search-probation-and-prison-with-user-lao-response.json"), JsonCompareMode.STRICT))
    }

    @Test
    fun `an obo user has a redaction applied to v2 person search`() {
      postToApiWithCN(v2BasePath, v2Request, "obo-unsigned-verified", oboValue = createUnsignedJwt())
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-search-v2-redacted.json"), JsonCompareMode.STRICT))
    }

    @Test
    fun `an obo user does not have a delius account for a v1 person search`() {
      manageUsersMockServer.stubForGet(
        "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundNoDeliusUsernameResponse.json",
        ).readText(),
      )

      val queryParams = "first_name=$firstName&last_name=$lastName"
      callApiWithCN("$basePath?$queryParams", "obo-unsigned-verified", oboValue = createUnsignedJwt())
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-search-prison-only.json"), JsonCompareMode.STRICT))
    }

    @Test
    fun `an obo user does not have a delius account for a v2 person search`() {
      manageUsersMockServer.stubForGet(
        "/users/search?name=testName&authSources=nomis&authSources=delius&authSources=azuread",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/UserFoundNoDeliusUsernameResponse.json",
        ).readText(),
      )

      postToApiWithCN(v2BasePath, v2Request, "obo-unsigned-verified", oboValue = createUnsignedJwt())
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-search-v2-prison-only.json"), JsonCompareMode.STRICT))
    }

    @Test
    fun `an obo user does not have the nomis role for a v2 person search`() {
      manageUsersMockServer.stubForGet(
        "/prisonusers/by-email/testName/details",
        File(
          "src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/manageUsers/fixtures/PrisonUsersResponseNoRole.json",
        ).readText(),
      )

      probationAccessMockServer.stubForPost(
        "/user/DELIUS_USER/access",
        defaultObjectMapper.writeValueAsString(listOf("A669783", "Z163457")),
        File("src/test/kotlin/uk/gov/justice/digital/hmpps/hmppsintegrationapi/gateways/probationAccess/fixtures/CheckUserAccessResponseNoLao.json").readText(),
      )

      postToApiWithCN(v2BasePath, v2Request, "obo-unsigned-verified", oboValue = createUnsignedJwt())
        .andExpect(status().isOk)
        .andExpect(content().json(getExpectedResponse("person-search-v2-probation-only.json"), JsonCompareMode.STRICT))
    }
  }
}
