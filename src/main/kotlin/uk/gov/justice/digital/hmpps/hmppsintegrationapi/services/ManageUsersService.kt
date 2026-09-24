package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsPrisonRole

@Service
class ManageUsersService(
  private val manageUsersGateway: ManageUsersGateway,
) {
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

  fun hasPrisonRole(
    emailAddress: String,
    role: HmppsPrisonRole,
  ): Boolean {
    val prisonUsersResponse = manageUsersGateway.getPrisonUsersByEmail(emailAddress)
    prisonUsersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }
    val roles =
      prisonUsersResponse.data?.filter { it.enabled }?.flatMap { prisonUser ->
        prisonUser.dpsRoleCodes
      } ?: emptyList()
    return roles.contains(role.name)
  }
}
