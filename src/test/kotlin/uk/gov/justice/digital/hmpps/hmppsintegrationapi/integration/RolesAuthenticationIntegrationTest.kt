package uk.gov.justice.digital.hmpps.hmppsintegrationapi.integration

import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.roleconfig.roles
import kotlin.test.assertTrue

@TestPropertySource(properties = ["feature-flag.gateway-cache-enabled=false"])
class RolesAuthenticationIntegrationTest : IntegrationTestBase() {
  @Test
  fun `all endpoints in all roles are authenticated and exist`() {
    val allRoles = roles
    val responses =
      allRoles
        .filter { !(it.value.permissions!!.any { r -> r.contains(".*") || r.contains("[^/]") }) }
        .flatMap { role ->
          val roleEndpoints = role.value.permissions!!
          roleEndpoints.map { endpoint ->
            val endpointCrn = fillEndpoint(endpoint)
            callApiWithCN(endpointCrn, role.value.name!!).andReturn().response
          }
        }

    val listOfIssues =
      responses.filter { response ->
        response.status == HttpStatus.FORBIDDEN.value() &&
          !response.contentAsString.contains("No static resource")
      }
    assertTrue(listOfIssues.isEmpty(), "Issues found with ${listOfIssues.map { it.contentAsString }} ")
  }

  @Test
  fun `all endpoints in all roles are not authenticated to any other endpoints`() {
    val allRoles = roles
    val allEndpoints = roles.getValue("all-endpoints")
    val responses =
      allRoles
        .filter { !(it.value.permissions!!.any { r -> r.contains(".*") || r.contains("[^/]") }) }
        .flatMap { role ->
          val roleEndpoints = ((role.value.permissions!!.toSet() subtract allEndpoints.permissions!!.toSet()) + (allEndpoints.permissions!!.toSet() subtract role.value.permissions!!.toSet())).toList()
          roleEndpoints.map { endpoint ->
            val endpointCrn = fillEndpoint(endpoint)
            callApiWithCN(endpointCrn, role.value.name!!).andReturn().response
          }
        }

    val listOfIssues = responses.filter { response -> response.status != HttpStatus.FORBIDDEN.value() }
    assertTrue(listOfIssues.isEmpty(), "Issues found with ${listOfIssues.map { it.contentAsString }} ")
  }

  private fun fillEndpoint(endpoint: String): String =
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
}
