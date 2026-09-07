package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.NomisRole

@Service
class ManageUsersService(
  private val manageUsersGateway: ManageUsersGateway,
) {
  companion object {
    const val NOMIS_AUTH_SOURCE = "nomis"
    const val NOMIS_GLOBAL_SEARCH_ROLE = "GLOBAL_SEARCH"
  }

  fun usernameExists(
    username: String,
    authSources: List<String>,
  ): Boolean {
    val usersResponse = manageUsersGateway.findUser(username, authSources)
    usersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }
    return usersResponse.data?.content?.any { it.enabled && !it.locked } ?: false
  }

  fun hasApplicableRole(username: String): Boolean {
    val usersResponse = manageUsersGateway.findUser(username, listOf(NOMIS_AUTH_SOURCE))
    usersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} findUser failed with error: ${it.type.name}")
    }

    val roles =
      usersResponse.data?.content?.filter { !it.locked }?.flatMap { user ->
        val rolesResponse = manageUsersGateway.getRoles(user.username)
        rolesResponse.errors.forEach {
          throw RuntimeException("Call to ${it.causedBy.name} getRoles failed with error: ${it.type.name}")
        }
        rolesResponse.data ?: emptyList()
      } ?: emptyList()

    return roles.contains(NomisRole(NOMIS_GLOBAL_SEARCH_ROLE))
  }
}
