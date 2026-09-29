package uk.gov.justice.digital.hmpps.hmppsintegrationapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.gateways.ManageUsersGateway
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsPrisonRole
import uk.gov.justice.digital.hmpps.hmppsintegrationapi.models.manageUsers.HmppsPrisonUser

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

  fun getActivePrisonUsersByEmail(emailAddress: String): List<HmppsPrisonUser> {
    val prisonUsersResponse = manageUsersGateway.getPrisonUsersByEmail(emailAddress)
    prisonUsersResponse.errors.forEach {
      throw RuntimeException("Call to ${it.causedBy.name} failed with error: ${it.type.name}")
    }
    return prisonUsersResponse.data?.filter { it.enabled } ?: emptyList()
  }

  fun hasPrisonRole(
    emailAddress: String,
    role: HmppsPrisonRole,
  ): Boolean {
    val users = getActivePrisonUsersByEmail(emailAddress)
    val roles =
      users.flatMap { prisonUser ->
        prisonUser.dpsRoleCodes
      }
    return roles.contains(role.name)
  }

  fun userCaseload(emailAddress: String): List<String> {
    val users = getActivePrisonUsersByEmail(emailAddress)
    val caseload =
      users
        .flatMap { user ->
          val response = manageUsersGateway.getPrisonUserCaseload(user.username)
          response.errors.forEach { err ->
            throw RuntimeException("Call to getPrisonUserCaseload in ${err.causedBy.name} failed with error: ${err.type.name}")
          }
          response.data?.caseloads ?: emptyList()
        }.mapNotNull { it.id }
    return caseload
  }
}
