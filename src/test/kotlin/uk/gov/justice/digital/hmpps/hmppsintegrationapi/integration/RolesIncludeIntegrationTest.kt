package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import kotlin.collections.iterator

class RolesIncludeIntegrationTest : IntegrationTestBase() {
  @Test
  fun `should get 200 when path is in the role includes`() {
    callApiWithCN("/v1/prison/prisoners/$nomsId", "private-prison-only")
      .andExpect(status().isOk)
  }

  @Test
  fun `should get 403 when path is not in role includes`() {
    callApiWithCN("/path-not-available-to-private-prison", "private-prison-only")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `reference-data-only should be able to access reference data`() {
    callApiWithCN("/v1/hmpps/reference-data", "reference-data-only-user")
      .andExpect(status().isOk)
  }

  @Test
  fun `reference-data-only should not be able to access other endpoint`() {
    callApiWithCN("/v1/prison/prisoners?first_name=Test", "reference-data-only-user")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `person-search-no-access-client should NOT be able to access search endpoint`() {
    callApiWithCN("/v1/persons?first_name=John", "person-search-no-access-client")
      .andExpect(status().isForbidden)
  }

  @Test
  fun `person-search-no-access-client should be able to access single persons endpoint`() {
    callApiWithCN("/v1/persons/$crn", "person-search-no-access-client")
      .andExpect(status().isOk)
  }

  @Test
  fun `person-search-no-access-client should be able to access persons address endpoint`() {
    callApiWithCN("/v1/persons/$crn/addresses", "person-search-no-access-client")
      .andExpect(status().isOk)
  }

  @Test
  fun `test all role endpoints to all mentiond path`() {
    val allRoles = roles
    val allEndpoints = roles.getValue("all-endpoints")
    for (role in allRoles) {
      var roleEndpoints = allEndpoints.permissions!!

      if (roleEndpoints.contains("{") && roleEndpoints.contains("}")) {
        val roleRegex = role.value.permissions!!.map { Regex(it) }
        roleEndpoints =
          roleEndpoints.filter { canonical ->
            val canonicalAsRegex = canonicalToRegex(canonical)
            roleRegex.any { roleRegex ->
              roleRegex.pattern.replace("[^/]*$", ".*") == canonicalAsRegex.pattern
            }
          }
      }
      for (endpoint in roleEndpoints) {
        val endpointCrn =
          endpoint
            .replace("{hmppsId}", crn)
            .replace("{scheduleId}", "123456")
            .replace("{activityId}", "1162")
            .replace("{contactId}", "123456")
            .replace("{nomisNumber}", nomsId)
            .replace("{id}", "123")
            .replace("{prisonId}", "MDI")
            .replace("{contactEventId}", "123")
            .replace("{jobId}", "123")
            .replace("{key}", "123")
            .replace("{accountCode}", "123")
            .replace("{clientUniqueRef}", "123")
            .replace("{clientReference}", "123")
            .replace("{visitReference}", "123")
            .replace("{eventNumber}", "123")
        callApiWithCN(endpointCrn, role.value.name!!)
          .andExpect { result -> assert(result.response.status != HttpStatus.FORBIDDEN.value() || result.response.status != HttpStatus.NOT_FOUND.value()) }
      }
    }
  }

  private fun canonicalToRegex(canonical: String): Regex {
    val pattern = canonical.replace(Regex("\\{[^/]+}"), ".*")
    return Regex(pattern)
  }
}
